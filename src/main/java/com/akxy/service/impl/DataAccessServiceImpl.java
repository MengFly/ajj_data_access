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

import java.util.*;
import java.util.stream.Collectors;


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
    private ConnStatusMapper connStatusMapper;
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
    DataUtil dataUtil;


    @Override
    public void copyDBToLocal(String customDB, String mineName) {
        List<Stress> listStress = localCacheService.getMidStressCache(customDB);
        List<Quake> listQuakes = localCacheService.getMidQuakeCache(customDB);

        if (!listStress.isEmpty()) {
            log.info(">> [{}-{}] 开始拷贝应力数据", customDB, mineName);
            TaskUtil.getInstance().splitTaskExec(listStress, 500, (stresses, i) -> {
                DynamicDataSourceContextHolder.setDataSource("copy");
                stressCopyMapper.copyStress(stresses);
            });
            log.info(">> [{}-{}] 应力数据拷贝完成", customDB, mineName);
        }

        if (!listQuakes.isEmpty()) {
            log.info(">> [{}-{}] 开始拷贝微震数据", customDB, mineName);
            TaskUtil.getInstance().splitTaskExec(listQuakes, 500, (quakes, i) -> {
                DynamicDataSourceContextHolder.setDataSource("copy");
                quakeCopyMapper.copyQuake(quakes);
            });
            log.info(">> [{}-{}] 微震数据拷贝完成", customDB, mineName);
        }
    }

    @Override
    public void configArea(String primaryDB, String customDB, String mineName) {
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<Area> existAreas = localCacheService.getMineAreaCache(customDB); // 目前子矿区中的工作面列表
        List<String> areaNames = localCacheService.getMidAreaNameCache(customDB).stream().distinct().collect(Collectors.toList());
        List<Long> areaIds = existAreas.stream().map(Area::getId).collect(Collectors.toList());
        int addedAreaCount = 0;
        if (areaIds.isEmpty()) {
            addedAreaCount++;
            Area area = Area.newInstance(AreaUtil.getAllMineId(customDB), "全矿", "0", "0");
            areaMapper.insertData(area);
            areaIds.add(0, area.getId());
        }
        for (String areaName : areaNames) {
            // 如果此工作面不存在于矿区数据库中才进行存储，id为（最大id+1）
            boolean flag = existAreas.stream().anyMatch(a -> areaName.equals(a.getName()));
            if (!flag) {
                addedAreaCount++;
                Area area = Area.newInstance(areaIds.get(0) + 1, areaName, "1", " ");
                areaMapper.insertData(area);
                areaIds.add(0, area.getId());
            }
        }
        if (addedAreaCount > 0) {
            log.info(">> [{}-{}] 新增工作面({})个", customDB, mineName, addedAreaCount);
            // 由于新增了工作面，所以缓存中的工作面也要更新
            localCacheService.resetMineAreaCache(customDB);
        } else {
            log.info(">> [{}-{}] 无新增工作面", customDB, mineName);
        }
    }

    @Override
    public void readAndCalculateStress(String primaryDB, String customDB, String mineName) {
        List<Stress> listStress = localCacheService.getMidStressCache(customDB);
        if (listStress.isEmpty()) {
            return;
        }
        log.info(">> [{}-{}] 开始处理应力数据({})条", customDB, mineName, listStress.size());
        long startTime = System.currentTimeMillis();
        List<StressMeasurePoint> stressMeasurePoints = localCacheService.getMinePointCache(customDB);
        List<Area> areas = localCacheService.getMineAreaCache(customDB);
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<StressDataInfo> stressDataInfos = dataUtil.optionStress(customDB, areas, listStress, stressMeasurePoints);
        if (!stressDataInfos.isEmpty()) {
            try {
                stressDataInfoMapper.deleteDataInfos(stressDataInfos);// 先删除再新增，防止数据重复
                stressDataInfoMapper.insertGroupDataInfo(stressDataInfos);
            } catch (Exception e) {
                log.error("INSERT 主键重复=>{},{}", customDB, e);
            }
        }
        stressMeasurePoints = localCacheService.getMinePointCache(customDB);
        writeToHiMine(customDB, areas, stressMeasurePoints, listStress);
        writeToCurMine(primaryDB, customDB, listStress);

        DynamicDataSourceContextHolder.setDataSource(primaryDB);
        int deleteCountStress = stressMapper.deleteGroupData(listStress);
        log.info(">> [{}-{}] 删除中间库应力数据({})条", customDB, mineName, deleteCountStress);


        // 上面已经存储完了应力信息
        try {
            localCacheService.resetStressDataCache(customDB);
            DynamicDataSourceContextHolder.setDataSource(customDB);
            List<StressDataInfo> nowStressDataInfos = localCacheService.getStressDataCache(customDB);
            if (nowStressDataInfos.size() != 0) {
                List<StressTopDataInfo> stressTopDataInfos = stressTopDataInfoMapper.stressTopDataInfos();
                dataUtil.writeToTopByPoints(nowStressDataInfos, stressTopDataInfos, stressMeasurePoints, customDB);
            }
        } catch (Exception e) {
            log.info("STRESS TOP WRITER EXCEPTION=>{},{}", customDB, e);
        }
        log.info(">> [{}-{}] 应力数据处理完毕，耗时 {} mms", customDB, mineName, (System.currentTimeMillis() - startTime));
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public void readAndCalculateQuake(String primaryDB, String customDB, String mineName) {
        List<Quake> quakes = localCacheService.getMidQuakeCache(customDB);
        if (quakes.isEmpty()) {
            return;
        }
        log.info(">> [{}-{}] 开始处理微震数据({})条", customDB, mineName, quakes.size());
        List<Area> areas = localCacheService.getMineAreaCache(customDB);
        try {
            List<PosResult> listINPosResult = new ArrayList<>();
            DynamicDataSourceContextHolder.setDataSource(customDB);
            String quakeWarnConfig = configMapper.getConfigInfo("WZ", "WARNING").getStrValue();
            for (Quake quake : quakes) {
                PosResult posResult = dataUtil.assemblePosResult(areas, customDB, quake, quakeWarnConfig);
                listINPosResult.add(posResult);
            }
            if (!listINPosResult.isEmpty()) {
                posResultMapper.insertGroupData(listINPosResult);
            }
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.error("微震写入异常=>{}", e.getMessage(), e);
        } finally {
            DynamicDataSourceContextHolder.setDataSource(primaryDB);
            int deleteCount = quakeMapper.deleteGroupData(quakes);
            log.info(">> [{}-{}] 微震中间库分析删除完毕，删除数据个数({})", customDB, mineName, deleteCount);
        }

    }

    @Override
    public void writeToCurMine(String primaryDB, String customDB, List<Stress> listStress) {
        List<CurMineInfo> curMineInfos = localCacheService.getCurMineCache(customDB);
        List<Area> areas = localCacheService.getMineAreaCache(customDB);
        List<StressMeasurePoint> measurePoints = localCacheService.getMinePointCache(customDB);
        listStress.stream().filter(stress -> stress != null && stress.getValue() >= 9).forEach(stress -> {
            CurMineInfo curMineInfo = dataUtil.getCurMineInfo(customDB, areas, measurePoints, stress);
            DynamicDataSourceContextHolder.setDataSource(customDB);
            boolean flag = curMineInfos.stream().anyMatch(
                    curMine -> curMine.getAreaId().equals(curMineInfo.getAreaId()) && curMine.getMpId().equals(curMineInfo.getMpId()));
            if (flag) {
                curMineInfoMapper.updateCurMine(curMineInfo);
            } else {
                curMineInfoMapper.writeToCurMine(curMineInfo);
            }
            DynamicDataSourceContextHolder.restoreDataSource();
        });
        localCacheService.resetCurMineCache(customDB);
    }

    @Override
    public void writeToHiMine(String customDB, List<Area> areas, List<StressMeasurePoint> stressMeasurePoints,
                              List<Stress> stresses) {
        DynamicDataSourceContextHolder.setDataSource(customDB);
        // 筛选出要预警的stress信息
        List<Stress> warnStress = stresses.stream()
                .filter(stress -> stress != null && stress.getValue() >= 9).collect(Collectors.toList());

        List<HiMineInfo> listHimineInfos = new ArrayList<>();
        for (Stress stress : warnStress) {
            HiMineInfo hiMineInfo = dataUtil.getHiMineInfo(customDB, stress, stressMeasurePoints, areas);
            listHimineInfos.add(hiMineInfo);
        }
        if (!listHimineInfos.isEmpty()) {
            DynamicDataSourceContextHolder.setDataSource(customDB);
            hiMineInfoMapper.insertGroupHi(listHimineInfos);
        }
    }

    @Override
    public void updatePointTime(String primaryDB, String customDB, String mineName) {
        try {
            DynamicDataSourceContextHolder.setDataSource(customDB);
            List<StressMeasurePoint> listUPPoints = new ArrayList<>();
            List<StressDataInfo> stressDataInfos = localCacheService.getStressDataCache(customDB);
            List<StressMeasurePoint> stressMeasurePoints = localCacheService.getMinePointCache(customDB);
            for (StressMeasurePoint stressMeasurePoint : stressMeasurePoints) {
                List<StressDataInfo> stressDataInfosList = stressDataInfos.stream()
                        .filter(e -> e.getMpId().equals(stressMeasurePoint.getId()))
                        .sorted(Comparator.comparing(StressDataInfo::getAcquisitionTime)).collect(Collectors.toList());
                Collections.reverse(stressDataInfosList);
                if (stressDataInfos.size() != 0) {
                    stressMeasurePoint.setFromTime(stressDataInfos.get(stressDataInfos.size() - 1).getAcquisitionTime());
                    stressMeasurePoint.setToTime(stressDataInfos.get(0).getAcquisitionTime());
                    listUPPoints.add(stressMeasurePoint);
                }
            }
            if (listUPPoints.size() != 0) {
                DynamicDataSourceContextHolder.setDataSource(customDB);
                stressMeasurePointMapper.updateGroupData(listUPPoints);
                // 更新了测点之后要更新缓存中的测点信息
                localCacheService.resetMinePointCache(customDB);
            }
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.info(">> [{}-{}]  EXCEPTION=>{}", customDB, mineName, e);
        }
    }

    @Override
    public void writeNotExistsMeasurePoint(String customDB, String mineName) {
        List<Area> areas = localCacheService.getMineAreaCache(customDB);
        List<StressMeasurePoint> stressMeasurePoints = localCacheService.getMinePointCache(customDB);
        List<Stress> stresses = localCacheService.getMidStressCache(customDB);
        DynamicDataSourceContextHolder.setDataSource(customDB);

        // 找到没有录入在子矿区数据库中的测点
        List<StressMeasurePoint> notExistsMeasurePoint = DataUtil.optionPoint(stressMeasurePoints, areas, stresses, customDB);
        log.info(">> [{}-{}] 无新增测点", customDB, mineName);
        if (notExistsMeasurePoint.isEmpty()) return;

        // 对测点id进行排序，方便下面取id的最大值
        List<Long> sortedMpIds = stressMeasurePoints.stream().map(StressMeasurePoint::getId)
                .sorted().collect(Collectors.toList());
        Collections.reverse(sortedMpIds);

        List<String> signList = new ArrayList<>();
        DynamicDataSourceContextHolder.setDataSource(customDB);
        notExistsMeasurePoint.forEach(point -> {
            if (sortedMpIds.size() == 0) {
                point.setId(Long.valueOf(customDB.substring(customDB.lastIndexOf("0") + 1) + "0001"));
            } else {
                point.setId(sortedMpIds.get(0) + 1);
            }
            if (!signList.contains(point.getTunnelName() + point.getDepth() + point.getDistance())) {
                stressMeasurePointMapper.writeMeasurePoint(point);
                signList.add(point.getTunnelName() + point.getDepth() + point.getDistance());
            }
            sortedMpIds.add(0, point.getId());
        });
        log.info(">> [{}-{}] 新增测点({})个", customDB, mineName, notExistsMeasurePoint.size());
        localCacheService.resetMinePointCache(customDB);
    }

    @Override
    public void writeToPlatform(String primaryDB, String customDB, String mineName) {
        List<Mine> mines = localCacheService.getMidMineCache(customDB);
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<ConnStatus> listConStatus = new ArrayList<>();
        List<ConnTopStatus> listINTopStatus = new ArrayList<>();
        List<ConnTopStatus> listUPTopStatus = new ArrayList<>();
        DynamicDataSourceContextHolder.setDataSource(customDB);
        Date stressTopNewDate = stressTopDataInfoMapper.findNewDate();
        int stressTopTimeOut = Integer.parseInt(configMapper.getConfigInfo("TIME", "STRESSTIMEOUT").getStrValue());

        Date quakeTopNewDate = posResultMapper.findNewDate();
        int quakeTopTimeOut = Integer.parseInt(configMapper.getConfigInfo("TIME", "QUAKETIMEOUT").getStrValue());
        for (Mine mine : mines) {
            ConnStatus stressConnStatu = dataUtil.getStressConStatus(customDB, mine, stressTopNewDate, stressTopTimeOut);
            ConnStatus quakeConnStatu = dataUtil.getQuakeConStatus(customDB, mine, quakeTopNewDate, quakeTopTimeOut);
            if (stressConnStatu != null) {
                listConStatus.add(stressConnStatu);
                // 把单个状态信息与Top表对比
                ConnTopStatus stresssConnTopStatus = new ConnTopStatus();
                BeanUtils.copyProperties(stressConnStatu, stresssConnTopStatus);
                boolean isContains = localCacheService.getMineConnTopStatusCache(customDB)
                        .stream()
                        .anyMatch(e -> e.getMineCode().equals(stresssConnTopStatus.getMineCode())
                                && e.getType().equals(stresssConnTopStatus.getType()));
                if (isContains) {
                    listUPTopStatus.add(stresssConnTopStatus);
                } else {
                    listINTopStatus.add(stresssConnTopStatus);
                }
            }
            if (quakeConnStatu != null) {
                listConStatus.add(quakeConnStatu);
                // 把单个状态信息与Top表对比
                ConnTopStatus quakeConnTopStatus = new ConnTopStatus();
                BeanUtils.copyProperties(quakeConnStatu, quakeConnTopStatus);
                boolean isContains = localCacheService.getMineConnTopStatusCache(customDB)
                        .stream().anyMatch(c -> Objects.equals(quakeConnTopStatus.getMineCode(), c.getMineCode())
                                && c.getType().equals(quakeConnTopStatus.getType()));
                if (isContains) {
                    listUPTopStatus.add(quakeConnTopStatus);
                } else {
                    listINTopStatus.add(quakeConnTopStatus);
                }
            }
        }
//        log.info("FENGYUANSHIYE=>{}", listUPTopStatus);
        DynamicDataSourceContextHolder.setDataSource("1000");
        // 连接历史表批量插入
        if (listConStatus.size() != 0) {
            connStatusMapper.insertGroupData(listConStatus);
        }
        if (listINTopStatus.size() != 0) {
            connTopStatusMapper.insertGroupData(listINTopStatus);
        }
        if (listUPTopStatus.size() != 0) {
            listUPTopStatus.forEach(top -> {
                connTopStatusMapper.updateConnTop(top);
            });
        }
        log.info(">> [{}-{}] 连接状态更新完成,新增Top状态({})，更新Top状态({})",
                customDB, mineName, listINTopStatus.size(),
                listUPTopStatus.size());
        localCacheService.resetMineConnTopStatusCache(customDB);
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public boolean hasNeedAnalysisData() {
        return stressMapper.stressCount() != 0 || quakeMapper.quakeCount() != 0;
    }
}
