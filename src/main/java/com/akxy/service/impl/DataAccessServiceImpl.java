package com.akxy.service.impl;

import com.akxy.common.DataUtil;
import com.akxy.common.StressMeasurePointUpdateItem;
import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.*;
import com.akxy.service.IDataAccessService;
import com.akxy.service.ILocalCacheService;
import com.akxy.util.AreaUtil;
import com.akxy.util.DateUtil;
import com.akxy.util.ParseUtil;
import com.akxy.util.StressUtil;
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
    //    @Autowired
//    private CurMineInfoMapper curMineInfoMapper;
//    @Autowired
//    private HiMineInfoMapper hiMineInfoMapper;
    @Autowired
    private PosResultMapper posResultMapper;
    @Autowired
    private StressTopDataInfoMapper stressTopDataInfoMapper;
    @Autowired
    private ConfigMapper configMapper;
    @Autowired
    private MinenewesttimeMapper minenewesttimeMapper;
    @Autowired
    private QuakeCopyMapper quakeCopyMapper;
    @Autowired
    private OrganMineMapper organMineMapper;

    @Autowired
    private ILocalCacheService localCacheService;
    @Autowired
    private DataUtil dataService;

    @Override
    public void configArea(String primaryDb, String customDb) {
        List<String> areaNames = localCacheService.getMidAreaNameCache(customDb);
        int addedAreaCount = 0;
        // 如果没有存工作面信息，添加一个全矿信息
        DynamicDataSourceContextHolder.setDataSource(customDb);
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
            log.info(">> 新增工作面({})个", addedAreaCount);
        } else {
            log.info(">> 无新增工作面");
        }
    }

    @Override
    public void readAndCalculateStress(String primaryDb, String customDb) {
        List<Stress> listStress = localCacheService.getMidStressCache(customDb);
        if (listStress.isEmpty()) {
            return;
        }
        log.info(">> 开始处理应力数据({})条", listStress.size());
        long startTime = System.currentTimeMillis();
        List<StressDataInfo> stressDataInfos = dataService.optionAllStress(customDb, listStress);

        List<StressDataInfo> savedStress = saveStressHistoryData(customDb, stressDataInfos);

        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        int deleteCountStress = stressMapper.deleteGroupData(listStress);
        log.info(">> 删除中间库应力数据({})条", deleteCountStress);
        DynamicDataSourceContextHolder.restoreDataSource();

        // 如果上面存储了新的应力信息，才进行存储更新应力的top表信息以及后续的操作
        if (savedStress.isEmpty()) {
            log.info(">> 应力数据处理完毕，耗时 {} mms", (System.currentTimeMillis() - startTime));
            return;
        }

        dataService.writeToTopByPoints(customDb, savedStress);
//        writeWarnMineInfo(customDb, savedStress);
        updatePointTimeAndName(customDb, savedStress);
        log.info(">> 应力数据处理完毕，耗时 {} mms", (System.currentTimeMillis() - startTime));
    }

    private List<StressDataInfo> saveStressHistoryData(String customDb, List<StressDataInfo> stressDataInfos) {
        // 空值和应力值小于30的才进行存储
        List<StressDataInfo> stressInfos = stressDataInfos.stream().filter(StressUtil::needSave).collect(Collectors.toList());

        if (stressInfos.isEmpty()) {
            log.info(">> 无符合的应力历史数据需要存储");
            return Collections.emptyList();
        }

        List<StressDataInfo> needSaveDataInfos = new ArrayList<>();
        Date newDate = stressTopDataInfoMapper.findNewDate();

        // 存储以工作面+测点为键的最大时间(因为存应力数据是要根据工作面和测点来判断的)
        Map<String, Timestamp> areaAndMpMaxTimeMap = new HashMap<>(32);

        if (newDate == null) {
            needSaveDataInfos = stressInfos;
        } else {
            for (StressDataInfo info : stressInfos) {
                if (info.getAcquisitionTime().getTime() > newDate.getTime()) {
                    needSaveDataInfos.add(info);
                } else {
                    String key = info.getAreaId() + "" + info.getMpId();
                    Timestamp areaAndMpMaxTime;
                    if (areaAndMpMaxTimeMap.containsKey(key)) {
                        areaAndMpMaxTime = areaAndMpMaxTimeMap.get(key);
                    } else {
                        areaAndMpMaxTime = stressTopDataInfoMapper.findMaxTimeBy(info.getAreaId(), info.getMpId());
                        if (areaAndMpMaxTime != null) {
                            areaAndMpMaxTimeMap.put(key, areaAndMpMaxTime);
                        }
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
            log.info(">> 应力历史需存入({})条，成功存入({})条", needSaveDataInfos.size(), insertSuccessCount);
        }
        log.info(">> 应力历史不符合存入条件({})条", (stressDataInfos.size() - needSaveDataInfos.size()));
        return needSaveDataInfos;
    }

    @Override
    public void readAndCalculateQuake(String primaryDb, String customDb) {
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
        log.info(">> 开始处理微震数据({})条", quakes.size());
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
                DynamicDataSourceContextHolder.setDataSource(customDb);
                int insertCount = posResultMapper.insertGroupData(needInsertPos);
                log.info(">> 成功存入微震({})条, 最新时间:{}", insertCount, ParseUtil.format(max.get().getCollectTime()));
            }
            DynamicDataSourceContextHolder.setDataSource(primaryDb);
            int deleteCount = quakeMapper.deleteGroupData(quakes);
            log.info(">> 微震中间库分析删除完毕，删除数据个数({})", deleteCount);
        } catch (Exception e) {
            log.error("微震写入异常=>{}", e.getMessage(), e);
        }
    }

//    public void writeWarnMineInfo(String customDb, List<StressDataInfo> stresses) {
//        // 应力值大于9的进行预警
//        List<MineInfo> warnMineInfo = stresses.stream()
//                .filter(stress -> stress.getPValue() >= 9)
//                .map(dataService::getHiMineInfo).collect(Collectors.toList());
//
//        log.info(">> 预警应力个数({})条", warnMineInfo.size());
//
//        if (!warnMineInfo.isEmpty()) {
//            DynamicDataSourceContextHolder.setDataSource(customDb);
//            hiMineInfoMapper.insertGroupHi(warnMineInfo);
//        }
//
//        DynamicDataSourceContextHolder.setDataSource(customDb);
//        // 保存top表信息
//        Map<String, MineInfo> insertMineInfo = new HashMap<>(32);
//        Map<String, MineInfo> updateMineInfo = new HashMap<>(32);
//        for (MineInfo mineInfo : warnMineInfo) {
//            String key = mineInfo.getAreaId() + "" + mineInfo.getMpId();
//            if (insertMineInfo.containsKey(key)) {
//                insertMineInfo.put(key, mineInfo);
//                continue;
//            }
//            if (updateMineInfo.containsKey(key)) {
//                updateMineInfo.put(key, mineInfo);
//                continue;
//            }
//            int count = curMineInfoMapper.countByAreaIdAndMpId(mineInfo.getAreaId(), mineInfo.getMpId());
//            if (count > 0) {
//                updateMineInfo.put(key, mineInfo);
//            } else {
//                insertMineInfo.put(key, mineInfo);
//            }
//        }
//        if (!updateMineInfo.isEmpty()) {
//            for (MineInfo mineInfo : updateMineInfo.values()) {
//                curMineInfoMapper.updateCurMine(mineInfo);
//            }
//            log.info(">> 更新CUR_MINE({})条", updateMineInfo.size());
//
//        }
//        if (!insertMineInfo.isEmpty()) {
//            curMineInfoMapper.insertAll(new ArrayList<>(insertMineInfo.values()));
//            log.info(">> 新增CUR_MINE({})条", insertMineInfo.size());
//        }
//        DynamicDataSourceContextHolder.setDataSource(customDb);
//
//    }

    public void updatePointTimeAndName(String customDb, List<StressDataInfo> stressDataInfos) {
        try {
            Map<Long, StressMeasurePointUpdateItem> mpIdMinTimeMap = new HashMap<>(16);
            Map<Long, StressMeasurePointUpdateItem> mpIdMaxTimeMap = new HashMap<>(16);
            List<StressDataInfo> filterList = stressDataInfos.stream()
                    .filter(stressDataInfo -> stressDataInfo.getAcquisitionTime() != null && stressDataInfo.getMpId() != 0)
                    .collect(Collectors.toList());
            for (StressDataInfo stressDataInfo : filterList) {
                Long mpId = stressDataInfo.getMpId();
                Date acquisitionTime = stressDataInfo.getAcquisitionTime();
                StressMeasurePointUpdateItem updateItem;
                if (!mpIdMaxTimeMap.containsKey(mpId)) {
                    updateItem = new StressMeasurePointUpdateItem();
                    updateItem.setId(mpId);
                    updateItem.setName(stressDataInfo.getMpName());
                    updateItem.setToTime(DateUtil.formatDate(acquisitionTime.getTime()));
                    updateItem.setLToTime(acquisitionTime.getTime());
                    mpIdMaxTimeMap.put(mpId, updateItem);
                } else {
                    updateItem = mpIdMaxTimeMap.get(mpId);
                    if (updateItem.getLToTime() < acquisitionTime.getTime()) {
                        updateItem.setToTime(DateUtil.formatDate(acquisitionTime.getTime()));
                        updateItem.setLToTime(acquisitionTime.getTime());
                        mpIdMaxTimeMap.put(mpId, updateItem);
                    }
                }

                if (!mpIdMinTimeMap.containsKey(mpId)) {
                    updateItem = new StressMeasurePointUpdateItem();
                    updateItem.setId(mpId);
                    updateItem.setFromTime(DateUtil.formatDate(acquisitionTime.getTime()));
                    updateItem.setLFromTime(acquisitionTime.getTime());
                    mpIdMinTimeMap.put(mpId, updateItem);
                } else {
                    updateItem = mpIdMinTimeMap.get(mpId);
                    if (updateItem.getLFromTime() > acquisitionTime.getTime()) {
                        updateItem.setFromTime(DateUtil.formatDate(acquisitionTime.getTime()));
                        updateItem.setLFromTime(acquisitionTime.getTime());
                        mpIdMinTimeMap.put(mpId, updateItem);
                    }
                }
            }

            DynamicDataSourceContextHolder.setDataSource(customDb);
            if (!mpIdMaxTimeMap.isEmpty()) {
                stressMeasurePointMapper.updateToTime(new ArrayList<>(mpIdMaxTimeMap.values()));
            }
            if (!mpIdMinTimeMap.isEmpty()) {
                stressMeasurePointMapper.updateFromTime(new ArrayList<>(mpIdMinTimeMap.values()));
            }
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.error("更新处理测点信息错误：{}", e.getMessage());
        }
    }

    @Override
    public void writeNotExistsMeasurePoint(String customDb) {
        List<Stress> stresses = localCacheService.getMidStressCache(customDb);
        // 找到没有录入在子矿区数据库中的测点
        List<StressMeasurePoint> notExistsMeasurePoint = dataService.optionPoint(stresses, customDb);
        if (notExistsMeasurePoint.isEmpty()) {
            log.info(">> 无新增测点");
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
        log.info(">> 新增测点({})个", notExistsMeasurePoint.size());
    }

    @Override
    public void writeToPlatform(String primaryDb, String customDb) {
        DynamicDataSourceContextHolder.setDataSource(customDb);
        Date stressTopNewDate = stressTopDataInfoMapper.findNewDate();
        int stressTopTimeOut = Integer.parseInt(configMapper.getConfigInfo("TIME", "STRESSTIMEOUT").getStrValue());

        Date quakeTopNewDate = posResultMapper.findNewDate();
        int quakeTopTimeOut = Integer.parseInt(configMapper.getConfigInfo("TIME", "QUAKETIMEOUT").getStrValue());
        String mineName = getMineName(customDb);
        ConnStatus stressConnStatu = dataService.getStressConStatus(customDb, mineName, stressTopNewDate, stressTopTimeOut);
        ConnStatus quakeConnStatu = dataService.getQuakeConStatus(customDb, mineName, quakeTopNewDate, quakeTopTimeOut);
        if (stressConnStatu != null) {
            saveOrUpdateTopStatus(stressConnStatu);
        }
        if (quakeConnStatu != null) {
            saveOrUpdateTopStatus(quakeConnStatu);
        }

        // 此逻辑只有在济矿的服务里面才要起作用
//        updateMineNewestTime(primaryDb, customDb, stressTopNewDate, quakeTopNewDate);
    }

    private void updateMineNewestTime(String primaryDb, String customDb, Date stressTopNewDate, Date quakeTopNewDate) {
        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        String stressMineCode = customDb + "YL";
        String quakeMineCode = customDb + "WZ";
        insertOrUpdateNewestTime(stressTopNewDate, stressMineCode);
        insertOrUpdateNewestTime(quakeTopNewDate, quakeMineCode);
    }

    private void insertOrUpdateNewestTime(Date newestTime, String mineCode) {
        if (newestTime != null) {
            MinenewestTime minenewestTime = minenewesttimeMapper.selectByPrimaryKey(mineCode);
            if (minenewestTime == null) {
                minenewestTime = new MinenewestTime();
                minenewestTime.setMineCode(mineCode);
                minenewestTime.setNewestTime(newestTime);
                minenewestTime.setMineName(getMineName(mineCode));
                minenewesttimeMapper.insert(minenewestTime);
            } else {
                if (minenewestTime.getNewestTime().getTime() < newestTime.getTime()) {
                    minenewestTime.setNewestTime(newestTime);
                    minenewesttimeMapper.updateByPrimaryKey(minenewestTime);
                }
            }
            log.info(">> 当前最新时间{}", ParseUtil.format(minenewestTime.getNewestTime()));
        }
    }

    private String getMineName(String mineCode) {
        String name = Thread.currentThread().getName();
        if ("UnKnown".equals(name)) {
            return ParseUtil.getOrDefault(organMineMapper.findNameById(mineCode), "UnKnown");
        } else {
            return name;
        }
    }

    private void saveOrUpdateTopStatus(ConnStatus connStatus) {
        ConnTopStatus connTopStatus = new ConnTopStatus();
        BeanUtils.copyProperties(connStatus, connTopStatus);
        if (connTopStatusMapper.countByMineCodeAndType(connTopStatus) > 0) {
            connTopStatusMapper.update(connTopStatus);
            log.info(">> 更新预警状态");
        } else {
            int insert = connTopStatusMapper.insert(connTopStatus);
            if (insert > 0) {
                log.info(">> 新增预警状态");
            } else {
                log.error(">> 新增预警状态失败");
            }

        }
    }
}
