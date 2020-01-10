package com.akxy.service.impl;

import com.akxy.common.DataUtil;
import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.*;
import com.akxy.service.IDataAccessService;
import com.akxy.service.ILocalCacheService;
import com.akxy.util.AreaUtil;
import com.akxy.util.TaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author wangp
 */
@Slf4j
@Service
public class DataAccessServiceImpl implements IDataAccessService {
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;
    @Autowired
    private StressMeasurePointMapper stressMeasurePointMapper;
    @Autowired
    private StressDataInfoMapper stressDataInfoMapper;
    @Autowired
    private ConnTopStatusMapper connTopStatusMapper;
    @Autowired
    private CurMineInfoMapper curMineInfoMapper;
    @Autowired
    private StressCopyMapper stressCopyMapper;
    @Autowired
    private QuakeCopyMapper quakeCopyMapper;
    @Autowired
    private PosResultMapper posResultMapper;
    @Autowired
    private StressTopDataInfoMapper stressTopDataInfoMapper;
    @Autowired
    private HiMineInfoMapper hiMineInfoMapper;
    @Autowired
    private ConfigMapper configMapper;

    @Autowired
    private ILocalCacheService localCacheService;
    @Autowired
    private DataUtil dataService;


    @Override
    public void copyDbToLocal(String customDb, String mineName) {
        List<Stress> listStress = localCacheService.getMidStressCache(customDb);
        List<Quake> listQuakes = localCacheService.getMidQuakeCache(customDb);

        if (!listStress.isEmpty()) {
            log.info(">> [{}-{}] 开始拷贝应力数据", customDb, mineName);
            TaskUtil.getInstance().splitTaskExec(listStress, 500, (stresses, i) -> {
                DynamicDataSourceContextHolder.setDataSource("copy");
                stressCopyMapper.copyStress(stresses);
            });
            log.info(">> [{}-{}] 应力数据拷贝完成", customDb, mineName);
        }

        if (!listQuakes.isEmpty()) {
            log.info(">> [{}-{}] 开始拷贝微震数据", customDb, mineName);
            TaskUtil.getInstance().splitTaskExec(listQuakes, 500, (quakes, i) -> {
                DynamicDataSourceContextHolder.setDataSource("copy");
                quakeCopyMapper.copyQuake(quakes);
            });
            log.info(">> [{}-{}] 微震数据拷贝完成", customDb, mineName);
        }
    }

    @Override
    public void configArea(String primaryDb, String customDb, String mineName) {
        // 目前子矿区中的工作面列表
        Map<String, Area> existAreas = localCacheService.getMineAreaCache(customDb);
        List<String> areaNames = localCacheService.getMidAreaNameCache(customDb);
        List<Long> areaIds = existAreas.values().stream().map(Area::getId).collect(Collectors.toList());
        int addedAreaCount = 0;
        if (areaIds.isEmpty()) {
            addedAreaCount++;
            Area area = Area.newInstance(AreaUtil.getAllMineId(customDb), "全矿", "0", "0");
            areaMapper.insertData(area);
            areaIds.add(0, area.getId());
        }
        for (String areaName : areaNames) {
            // 如果此工作面不存在于矿区数据库中才进行存储，id为（最大id+1）
            if (!existAreas.containsKey(areaName)) {
                addedAreaCount++;
                Area area = Area.newInstance(areaIds.get(0) + 1, areaName, "1", " ");
                areaMapper.insertData(area);
                areaIds.add(0, area.getId());
            }
        }
        if (addedAreaCount > 0) {
            log.info(">> [{}-{}] 新增工作面({})个", customDb, mineName, addedAreaCount);
            // 由于新增了工作面，所以缓存中的工作面也要更新
            localCacheService.resetMineAreaCache(customDb);
        } else {
            log.info(">> [{}-{}] 无新增工作面", customDb, mineName);
        }
    }

    @Override
    public void readAndCalculateStress(String primaryDb, String customDb, String mineName) {
        List<Stress> listStress = localCacheService.getMidStressCache(customDb);
        if (listStress.isEmpty()) {
            return;
        }
        log.info(">> [{}-{}] 开始处理应力数据({})条", customDb, mineName, listStress.size());
        long startTime = System.currentTimeMillis();
        List<StressDataInfo> stressDataInfos = dataService.optionAllStress(customDb, listStress);

        List<StressDataInfo> savedStress = saveStressHistoryData(customDb, mineName, stressDataInfos);
        // 如果上面存储了新的应力信息，才进行存储更新应力的top表信息
        if (!savedStress.isEmpty()) {
            localCacheService.resetStressDataCache(customDb);
            dataService.writeToTopByPoints(customDb, mineName, savedStress);
        }

        writeWarnMineInfo(customDb, mineName, stressDataInfos);

        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        int deleteCountStress = stressMapper.deleteGroupData(listStress);
        log.info(">> [{}-{}] 删除中间库应力数据({})条", customDb, mineName, deleteCountStress);

        log.info(">> [{}-{}] 应力数据处理完毕，耗时 {} mms", customDb, mineName, (System.currentTimeMillis() - startTime));
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    private List<StressDataInfo> saveStressHistoryData(String customDb, String mineName, List<StressDataInfo> stressDataInfos) {
        // 空值和应力值小于30的才进行存储
        List<StressDataInfo> stressInfos = stressDataInfos.stream()
                .filter(info -> info.getpValue() <= 30 && info.getAcquisitionTime() != null).collect(Collectors.toList());

        if (stressInfos.isEmpty()) {
            log.info(">> [{}-{}] 无符合的应力历史数据需要存储", customDb, mineName);
            return Collections.emptyList();
        }

        List<StressDataInfo> needSaveDataInfos = new ArrayList<>();
        Timestamp maxTime = stressDataInfoMapper.findMaxAcquisitionTime();
        // 存储以工作面+测点为键的最大时间(因为存应力数据是要根据工作面和测点来判断的)
        Map<String, Timestamp> areaAndMpMaxTimeMap = new HashMap<>(32);

        if (maxTime == null) {
            needSaveDataInfos = stressInfos;
        } else {
            for (StressDataInfo info : stressInfos) {
                if (info.getAcquisitionTime().getTime() > maxTime.getTime()) {
                    needSaveDataInfos.add(info);
                } else {
                    String key = info.getAreaId() + "" + info.getMpId();
                    Timestamp areaAndMpMaxTime;
                    if (areaAndMpMaxTimeMap.containsKey(key)) {
                        areaAndMpMaxTime = areaAndMpMaxTimeMap.get(key);
                    } else {
                        areaAndMpMaxTime = stressDataInfoMapper.findMaxAcquisitionTimeBy(info.getAreaId(), info.getMpId());
                        areaAndMpMaxTimeMap.put(key, areaAndMpMaxTime);
                    }
                    if (areaAndMpMaxTime == null || info.getAcquisitionTime().getTime() > areaAndMpMaxTime.getTime()) {
                        needSaveDataInfos.add(info);
                    }
                }
            }

        }
        if (!needSaveDataInfos.isEmpty()) {
            DynamicDataSourceContextHolder.setDataSource(customDb);
            int insertSuccessCount = stressDataInfoMapper.insertGroupDataInfo(needSaveDataInfos);
            log.info(">> [{}-{}] 应力历史需存入({})条，成功存入({})条", customDb, mineName, needSaveDataInfos.size(), insertSuccessCount);
        }
        log.info(">> [{}-{}] 应力历史不符合存入条件({})条", customDb, mineName, (stressDataInfos.size() - needSaveDataInfos.size()));
        return needSaveDataInfos;
    }

    @Override
    public void readAndCalculateQuake(String primaryDb, String customDb, String mineName) {
        List<Quake> quakes = localCacheService.getMidQuakeCache(customDb);
        if (quakes.isEmpty()) {
            return;
        }
        log.info(">> [{}-{}] 开始处理微震数据({})条", customDb, mineName, quakes.size());
        Map<String, Area> areas = localCacheService.getMineAreaCache(customDb);
        try {
            List<PosResult> needInsertPos = new ArrayList<>();
            DynamicDataSourceContextHolder.setDataSource(customDb);
            String quakeWarnConfig = configMapper.getConfigInfo("WZ", "WARNING").getStrValue();
            for (Quake quake : quakes) {

                Area orDefault = areas.getOrDefault(quake.getAreaname(), null);
                Long areaId = orDefault == null ? AreaUtil.getAllMineId(customDb) : orDefault.getId();
                PosResult posResult = dataService.assemblePosResult(areaId, quake, quakeWarnConfig);
                needInsertPos.add(posResult);
            }
            if (!needInsertPos.isEmpty()) {
                posResultMapper.insertGroupData(needInsertPos);
            }
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.error("微震写入异常=>{}", e.getMessage(), e);
        } finally {
            DynamicDataSourceContextHolder.setDataSource(primaryDb);
            int deleteCount = quakeMapper.deleteGroupData(quakes);
            log.info(">> [{}-{}] 微震中间库分析删除完毕，删除数据个数({})", customDb, mineName, deleteCount);
        }
    }

    public void writeWarnMineInfo(String customDb, String mineName, List<StressDataInfo> stresses) {
        // 应力值大于9的进行预警
        List<MineInfo> warnMineInfo = stresses.stream()
                .filter(stress -> stress.getpValue() >= 9)
                .map(dataService::getHiMineInfo).collect(Collectors.toList());

        log.info(">> [{}-{}] 预警应力个数({})条", customDb, mineName, warnMineInfo.size());

        if (!warnMineInfo.isEmpty()) {
            DynamicDataSourceContextHolder.setDataSource(customDb);
            hiMineInfoMapper.insertGroupHi(warnMineInfo);
        }

        DynamicDataSourceContextHolder.setDataSource(customDb);
        // 保存top表信息
        Map<String, MineInfo> insertMineInfo = new HashMap<>(32);
        Map<String, MineInfo> updateMineInfo = new HashMap<>(32);
        for (MineInfo mineInfo : warnMineInfo) {
            String key = mineInfo.getAreaId() + "" + mineInfo.getMpId();
            if (insertMineInfo.containsKey(key)) {
                insertMineInfo.put(key, mineInfo);
                continue;
            }
            if (updateMineInfo.containsKey(key)) {
                updateMineInfo.put(key, mineInfo);
                continue;
            }
            int count = curMineInfoMapper.countByAreaIdAndMpId(mineInfo.getAreaId(), mineInfo.getMpId());
            if (count > 0) {
                updateMineInfo.put(key, mineInfo);
            } else {
                insertMineInfo.put(key, mineInfo);
            }
        }
        if (!updateMineInfo.isEmpty()) {
            for (MineInfo mineInfo : updateMineInfo.values()) {
                curMineInfoMapper.updateCurMine(mineInfo);
            }
            log.info(">> [{}-{}] 更新CUR_MINE({})条", customDb, mineName, updateMineInfo.size());

        }
        if (!insertMineInfo.isEmpty()) {
            curMineInfoMapper.insertAll(new ArrayList<>(insertMineInfo.values()));
            log.info(">> [{}-{}] 新增CUR_MINE({})条", customDb, mineName, insertMineInfo.size());
        }
        DynamicDataSourceContextHolder.setDataSource(customDb);

    }

    @Override
    public void updatePointTime(String primaryDb, String customDb, String mineName) {
        try {
            List<StressMeasurePoint> needUpdatePoints = new ArrayList<>();
            List<StressDataInfo> stressDataInfos = localCacheService.getStressDataCache(customDb);
            List<StressMeasurePoint> stressMeasurePoints = localCacheService.getMinePointCache(customDb);
            for (StressMeasurePoint stressMeasurePoint : stressMeasurePoints) {
                List<StressDataInfo> stressDataInfosList = stressDataInfos.stream()
                        .filter(e -> e.getMpId().equals(stressMeasurePoint.getId()))
                        .sorted(Comparator.comparing(StressDataInfo::getAcquisitionTime)).collect(Collectors.toList());
                Collections.reverse(stressDataInfosList);
                if (stressDataInfos.size() != 0) {
                    stressMeasurePoint.setFromTime(stressDataInfos.get(stressDataInfos.size() - 1).getAcquisitionTime());
                    stressMeasurePoint.setToTime(stressDataInfos.get(0).getAcquisitionTime());
                    needUpdatePoints.add(stressMeasurePoint);
                }
            }
            if (needUpdatePoints.size() != 0) {
                DynamicDataSourceContextHolder.setDataSource(customDb);
                stressMeasurePointMapper.updateGroupData(needUpdatePoints);
                // 更新了测点之后要更新缓存中的测点信息
                localCacheService.resetMinePointCache(customDb);
            }
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.info(">> [{}-{}]  EXCEPTION=>{}", customDb, mineName, e);
        }
    }

    @Override
    public void writeNotExistsMeasurePoint(String customDb, String mineName) {
        List<StressMeasurePoint> stressMeasurePoints = localCacheService.getMinePointCache(customDb);
        List<Stress> stresses = localCacheService.getMidStressCache(customDb);
        // 找到没有录入在子矿区数据库中的测点
        List<StressMeasurePoint> notExistsMeasurePoint = dataService.optionPoint(stressMeasurePoints, stresses, customDb);
        log.info(">> [{}-{}] 无新增测点", customDb, mineName);
        if (notExistsMeasurePoint.isEmpty()) {
            return;
        }

        // 对测点id进行排序，方便下面取id的最大值
        List<Long> sortedMpIds = stressMeasurePoints.stream().map(StressMeasurePoint::getId)
                .sorted().collect(Collectors.toList());
        Collections.reverse(sortedMpIds);

        List<String> signList = new ArrayList<>();
        DynamicDataSourceContextHolder.setDataSource(customDb);
        notExistsMeasurePoint.forEach(point -> {
            if (sortedMpIds.size() == 0) {
                point.setId(Long.valueOf(customDb.substring(customDb.lastIndexOf("0") + 1) + "0001"));
            } else {
                point.setId(sortedMpIds.get(0) + 1);
            }
            if (!signList.contains(point.getTunnelName() + point.getDepth() + point.getDistance())) {
                stressMeasurePointMapper.writeMeasurePoint(point);
                signList.add(point.getTunnelName() + point.getDepth() + point.getDistance());
            }
            sortedMpIds.add(0, point.getId());
        });
        log.info(">> [{}-{}] 新增测点({})个", customDb, mineName, notExistsMeasurePoint.size());
        localCacheService.resetMinePointCache(customDb);
    }

    @Override
    public void writeToPlatform(String primaryDb, String customDb, String mineName) {
        List<Mine> mines = localCacheService.getMidMineCache(customDb);
        DynamicDataSourceContextHolder.setDataSource(customDb);
        Date stressTopNewDate = stressTopDataInfoMapper.findNewDate();
        int stressTopTimeOut = Integer.parseInt(configMapper.getConfigInfo("TIME", "STRESSTIMEOUT").getStrValue());

        Date quakeTopNewDate = posResultMapper.findNewDate();
        int quakeTopTimeOut = Integer.parseInt(configMapper.getConfigInfo("TIME", "QUAKETIMEOUT").getStrValue());
        for (Mine mine : mines) {
            ConnStatus stressConnStatu = dataService.getStressConStatus(customDb, mine, stressTopNewDate, stressTopTimeOut);
            ConnStatus quakeConnStatu = dataService.getQuakeConStatus(customDb, mine, quakeTopNewDate, quakeTopTimeOut);
            if (stressConnStatu != null) {
                saveOrUpdateTopStatus(stressConnStatu, customDb, mineName);
            }
            if (quakeConnStatu != null) {
                saveOrUpdateTopStatus(quakeConnStatu, customDb, mineName);
            }
        }
    }

    private void saveOrUpdateTopStatus(ConnStatus connStatus, String customDb, String mineName) {
        ConnTopStatus connTopStatus = new ConnTopStatus();
        BeanUtils.copyProperties(connStatus, connTopStatus);
        if (connTopStatusMapper.countByMineCodeAndType(connTopStatus) > 0) {
            connTopStatusMapper.update(connTopStatus);
            log.info(">> [{}-{}] 更新预警状态", customDb, mineName);
        } else {
            int insert = connTopStatusMapper.insert(connTopStatus);
            if (insert > 0) {
                log.info(">> [{}-{}] 新增预警状态", customDb, mineName);
            } else {
                log.error(">> [{}-{}] 新增预警状态失败", customDb, mineName);
            }

        }
    }

    @Override
    public boolean hasNeedAnalysisData() {
        return stressMapper.stressCount() != 0 || quakeMapper.quakeCount() != 0;
    }
}
