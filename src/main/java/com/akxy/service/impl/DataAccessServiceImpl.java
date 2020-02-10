package com.akxy.service.impl;

import com.akxy.common.DataUtil;
import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.*;
import com.akxy.service.IDataAccessService;
import com.akxy.service.ILocalCacheService;
import com.akxy.util.AreaUtil;
import com.akxy.util.ParseUtil;
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
    private PosResultMapper posResultMapper;
    @Autowired
    private StressTopDataInfoMapper stressTopDataInfoMapper;
    @Autowired
    private HiMineInfoMapper hiMineInfoMapper;
    @Autowired
    private ConfigMapper configMapper;
    @Autowired
    private MinenewesttimeMapper minenewesttimeMapper;
    @Autowired
    private QuakeCopyMapper quakeCopyMapper;

    @Autowired
    private ILocalCacheService localCacheService;
    @Autowired
    private DataUtil dataService;

    @Override
    public void configArea(String primaryDb, String customDb, String mineName) {
        List<String> areaNames = localCacheService.getMidAreaNameCache(customDb);
        DynamicDataSourceContextHolder.setDataSource(customDb);
        int addedAreaCount = 0;
        // 如果没有存工作面信息，添加一个全矿信息
        if (areaMapper.count() == 0) {
            Long mineId = Long.valueOf(customDb.substring(customDb.lastIndexOf("0") + 1) + "000");
            Area area = Area.newInstance(mineId, "全矿", "0", "0");
            areaMapper.insertData(area);
            addedAreaCount++;
        }
        long maxId = -1;
        if (!areaNames.isEmpty()) {
            maxId = areaMapper.maxId();
        }
        for (String areaName : areaNames) {
            // 如果工作面不存在，那么就创建新的工作面
            if (areaMapper.countByName(areaName) == 0) {
                Area area = Area.newInstance(++maxId, areaName, "1", "1");
                areaMapper.insertData(area);
                addedAreaCount++;
            }
        }
        if (addedAreaCount > 0) {
            log.info(">> [{}-{}] 新增工作面({})个", customDb, mineName, addedAreaCount);
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
            dataService.writeToTopByPoints(customDb, mineName, savedStress);
        }

        writeWarnMineInfo(customDb, mineName, stressDataInfos);

        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        int deleteCountStress = stressMapper.deleteGroupData(listStress);
        log.info(">> [{}-{}] 删除中间库应力数据({})条", customDb, mineName, deleteCountStress);

        log.info(">> [{}-{}] 应力数据处理完毕，耗时 {} mms", customDb, mineName, (System.currentTimeMillis() - startTime));
        DynamicDataSourceContextHolder.restoreDataSource();
        updatePointTime(customDb, mineName, stressDataInfos);
    }

    private List<StressDataInfo> saveStressHistoryData(String customDb, String mineName, List<StressDataInfo> stressDataInfos) {
        // 空值和应力值小于30的才进行存储
        List<StressDataInfo> stressInfos = stressDataInfos.stream()
                .filter(info -> info.getPValue() <= 30 && info.getAcquisitionTime() != null).collect(Collectors.toList());

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
        try {
            DynamicDataSourceContextHolder.setDataSource("copy");
            quakeCopyMapper.copyQuake(quakes);
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.error("Copy error", e);
        }
        log.info(">> [{}-{}] 开始处理微震数据({})条", customDb, mineName, quakes.size());
        try (AreaUtil areaUtil = new AreaUtil()) {
            List<PosResult> needInsertPos = new ArrayList<>();
            DynamicDataSourceContextHolder.setDataSource(customDb);
            String quakeWarnConfig = configMapper.getConfigInfo("WZ", "WARNING").getStrValue();
            for (Quake quake : quakes) {
                Long areaId = areaUtil.getId(quake.getAreaname(), customDb, areaMapper);
                PosResult posResult = dataService.assemblePosResult(areaId, quake, quakeWarnConfig);
                if (posResultMapper.count(posResult) == 0) {
                    needInsertPos.add(posResult);
                }
            }

            if (!needInsertPos.isEmpty()) {
                Optional<PosResult> max = needInsertPos.stream().max(Comparator.comparing(PosResult::getCollectTime));
                int insertCount = posResultMapper.insertGroupData(needInsertPos);
                log.info(">> [{}-{}] 成功存入微震({})条, 最新时间:{}",
                        customDb, mineName, insertCount, ParseUtil.format(max.get().getCollectTime()));
            }
            DynamicDataSourceContextHolder.setDataSource(primaryDb);
            int deleteCount = quakeMapper.deleteGroupData(quakes);
            log.info(">> [{}-{}] 微震中间库分析删除完毕，删除数据个数({})", customDb, mineName, deleteCount);
        } catch (Exception e) {
            log.error("微震写入异常=>{}", e.getMessage(), e);
        }
    }

    public void writeWarnMineInfo(String customDb, String mineName, List<StressDataInfo> stresses) {
        // 应力值大于9的进行预警
        List<MineInfo> warnMineInfo = stresses.stream()
                .filter(stress -> stress.getPValue() >= 9)
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

    public void updatePointTime(String customDb, String mineName, List<StressDataInfo> stressDataInfos) {
        try {
            Map<Long, StressMeasurePoint> mpIdMinTimeMap = new HashMap<>(16);
            Map<Long, StressMeasurePoint> mpIdMaxTimeMap = new HashMap<>(16);
            List<StressDataInfo> filterList = stressDataInfos.stream()
                    .filter(stressDataInfo -> stressDataInfo.getAcquisitionTime() != null && stressDataInfo.getMpId() != 0)
                    .collect(Collectors.toList());
            for (StressDataInfo stressDataInfo : filterList) {
                Long mpId = stressDataInfo.getMpId();
                Date acquisitionTime = stressDataInfo.getAcquisitionTime();
                StressMeasurePoint measurePoint;
                if (!mpIdMaxTimeMap.containsKey(mpId)) {
                    measurePoint = new StressMeasurePoint();
                    measurePoint.setToTime(acquisitionTime);
                    mpIdMaxTimeMap.put(mpId, measurePoint);
                } else {
                    measurePoint = mpIdMaxTimeMap.get(mpId);
                    if (measurePoint.getToTime().getTime() < acquisitionTime.getTime()) {
                        measurePoint.setToTime(acquisitionTime);
                        mpIdMaxTimeMap.put(mpId, measurePoint);
                    }
                }

                if (!mpIdMinTimeMap.containsKey(mpId)) {
                    measurePoint = new StressMeasurePoint();
                    measurePoint.setFromTime(acquisitionTime);
                    mpIdMinTimeMap.put(mpId, measurePoint);
                } else {
                    measurePoint = mpIdMinTimeMap.get(mpId);
                    if (measurePoint.getFromTime().getTime() > acquisitionTime.getTime()) {
                        measurePoint.setFromTime(acquisitionTime);
                        mpIdMinTimeMap.put(mpId, measurePoint);
                    }
                }
            }

            DynamicDataSourceContextHolder.setDataSource(customDb);
            stressMeasurePointMapper.updateToTime(new ArrayList<>(mpIdMaxTimeMap.values()));
            stressMeasurePointMapper.updateFromTime(new ArrayList<>(mpIdMinTimeMap.values()));
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.info(">> [{}-{}]  EXCEPTION=>{}", customDb, mineName, e);
        }
    }

    @Override
    public void writeNotExistsMeasurePoint(String customDb, String mineName) {
        List<Stress> stresses = localCacheService.getMidStressCache(customDb);
        // 找到没有录入在子矿区数据库中的测点
        List<StressMeasurePoint> notExistsMeasurePoint = dataService.optionPoint(stresses, customDb);
        if (notExistsMeasurePoint.isEmpty()) {
            log.info(">> [{}-{}] 无新增测点", customDb, mineName);
            return;
        }
        // 对测点id进行排序，方便下面取id的最大值
        DynamicDataSourceContextHolder.setDataSource(customDb);
        Long maxId = stressMeasurePointMapper.findMaxId();
        if (maxId == null || maxId == 0) {
            maxId = Long.valueOf(customDb.substring(customDb.lastIndexOf("0") + 1) + "0001");
        } else {
            maxId = maxId + 1;
        }
        DynamicDataSourceContextHolder.setDataSource(customDb);
        for (StressMeasurePoint stressMeasurePoint : notExistsMeasurePoint) {
            stressMeasurePoint.setId(maxId);
            stressMeasurePointMapper.writeMeasurePoint(stressMeasurePoint);
            maxId++;
        }
        log.info(">> [{}-{}] 新增测点({})个", customDb, mineName, notExistsMeasurePoint.size());
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

        // 此逻辑只有在济矿的服务里面才要起作用
//        updateMineNewestTime(primaryDb, customDb, mineName, stressTopNewDate, quakeTopNewDate);
    }

    private void updateMineNewestTime(String primaryDb, String customDb, String mineName, Date stressTopNewDate, Date quakeTopNewDate) {
        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        String stressMineCode = customDb + "YL";
        String quakeMineCode = customDb + "WZ";
        insertOrUpdateNewestTime(mineName, stressTopNewDate, stressMineCode);
        insertOrUpdateNewestTime(mineName, quakeTopNewDate, quakeMineCode);
    }

    private void insertOrUpdateNewestTime(String mineName, Date newestTime, String mineCode) {
        if (newestTime != null) {
            MinenewestTime minenewestTime = minenewesttimeMapper.selectByPrimaryKey(mineCode);
            if (minenewestTime == null) {
                minenewestTime = new MinenewestTime();
                minenewestTime.setMineCode(mineCode);
                minenewestTime.setNewestTime(newestTime);
                minenewestTime.setMineName(mineName);
                minenewesttimeMapper.insert(minenewestTime);
            } else {
                if (minenewestTime.getNewestTime().getTime() < newestTime.getTime()) {
                    minenewestTime.setNewestTime(newestTime);
                    minenewesttimeMapper.updateByPrimaryKey(minenewestTime);
                }
            }
            log.info(">> [{}-{}] 当前最新时间{}", mineCode, mineName, ParseUtil.format(minenewestTime.getNewestTime()));
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
