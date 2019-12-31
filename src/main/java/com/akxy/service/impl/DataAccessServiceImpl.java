package com.akxy.service.impl;

import com.akxy.common.DataUtil;
import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.*;
import com.akxy.service.IDataAccessService;
import com.akxy.util.AVGList;
import com.akxy.util.CopyUtils;
import com.akxy.util.TaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Slf4j
@SuppressWarnings("unchecked")
@Service
public class DataAccessServiceImpl implements IDataAccessService {

    public Map<String, Object> mapCache = new ConcurrentHashMap<>();
    private DataUtil dataUtil = new DataUtil();

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
    private ConfigMapper configMapper;
    @Autowired
    private MineMapper mineMapper;
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

    @Override
    public Map<String, Object> getMapCache(String primaryDB, String customDB) {
        try {
            DynamicDataSourceContextHolder.setDataSource("1000"); // 安监局平台数据库
            // 查询本矿区连接状态TOP信息
            mapCache.put("CONNTOPSTATUS" + customDB, connTopStatusMapper.getTopStatusByMineCode(customDB));

            DynamicDataSourceContextHolder.setDataSource(primaryDB);// 中间库数据库
            mapCache.put("STRESS" + customDB, stressMapper.readStressData(customDB));// 1000 条应力数据
            mapCache.put("QUAKE" + customDB, quakeMapper.readQuakeData(customDB)); // 1000 条微震信息
            mapCache.put("MINE" + customDB, mineMapper.listMines(customDB));// 本矿区信息
            mapCache.put("AREANAME" + customDB, stressMapper.getAllAreaName(customDB));// 应力数据中包含的工作面信息
            mapCache.put("PSIGN" + customDB, stressMapper.getPointSignList(customDB)); // 应力测点信息
            DynamicDataSourceContextHolder.restoreDataSource();

            DynamicDataSourceContextHolder.setDataSource(customDB);// 子矿区数据库
            mapCache.put("AREA" + customDB, areaMapper.getArea());// 所有工作面信息
            mapCache.put("STRDATA" + customDB, stressDataInfoMapper.getDataInfoCache());// 应力数据的最近1000条数据
            mapCache.put("POINT" + customDB, stressMeasurePointMapper.getAllPoint());// 所有测点信息
            mapCache.put("CURMINE" + customDB, curMineInfoMapper.getAllCurMine());//
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.error("MAPCACHE EXCEPTION=>{},{}", customDB, e);
        }
        return mapCache;
    }

    @Override
    public void copyDBToLocal(String customDB) {
        List<Stress> listStress = (List<Stress>) mapCache.get("STRESS" + customDB);
        List<Quake> listQuakes = (List<Quake>) mapCache.get("QUAKE" + customDB);

        if (!CollectionUtils.isEmpty(listStress)) {
            log.info(">> [{}] 开始拷贝应力数据", customDB);
            TaskUtil.getInstance().splitTaskExec(listStress, 500, (stresses, i) -> {
                DynamicDataSourceContextHolder.setDataSource("copy");
                stressCopyMapper.copyStress(stresses);
            });
            log.info(">> [{}] 应力数据拷贝完成", customDB);
        }

        if (!CollectionUtils.isEmpty(listQuakes)) {
            log.info(">> [{}] 开始拷贝微震数据", customDB);
            TaskUtil.getInstance().splitTaskExec(listQuakes, 500, (quakes, i) -> {
                DynamicDataSourceContextHolder.setDataSource("copy");
                quakeCopyMapper.copyQuake(quakes);
            });
            log.info(">> [{}] 微震数据拷贝完成", customDB);
        }
    }

    @Override
    public void configArea(String primaryDB, String customDB) {
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<Area> existAreas = (List<Area>) mapCache.get("AREA" + customDB);
        List<String> areaNames = (List<String>) mapCache.get("AREANAME" + customDB);
        areaNames = areaNames.stream().distinct().collect(Collectors.toList());
        List<Long> areaIds = new ArrayList<>();
        try {
            if (existAreas != null) {
                List<Long> idsList = existAreas.stream().map(Area::getId).collect(Collectors.toList());
                areaIds.addAll(idsList);
            }
            if (existAreas == null) {
                areaIds.add(0, Long.valueOf(customDB.substring(customDB.length() - 1, customDB.length()) + "000"));
            }
            Area area = new Area();
            StringBuffer minSign = new StringBuffer();
            minSign.append(customDB.substring(customDB.lastIndexOf("0") + 1, customDB.length()));
            area.setId(Long.valueOf(minSign.append("000").toString()));
            area.setName("全矿");
            area.setPosList(" ");
            area.setType("0");
            area.setIsmonitor((short) 1);
            area.setMemo("0");
            if (!areaIds.contains(area.getId())) {
                areaIds.add(0, area.getId());
//				areaNames.add(area.getName());
                areaMapper.insertData(area);
            }
            areaNames.forEach(e -> {
//				List<Area> areaTemp = existAreas.stream().filter(a -> e.equals(a.getName()))
//						.collect(Collectors.toList());
                boolean flag = existAreas.stream().anyMatch(a -> e.equals(a.getName()));
                if (flag == false) {
                    Area a = new Area();
                    a.setId(areaIds.get(0) + 1);
                    a.setName(e);
                    a.setPosList(" ");
                    a.setType("1");
                    a.setIsmonitor((short) 1);
                    a.setMemo(" ");
                    areaIds.add(0, area.getId());
                    areaMapper.insertData(a);
                    areaIds.add(0, a.getId());
                }
            });
            mapCache.put("AREA" + customDB, areaMapper.getArea());
        } catch (Exception e2) {
            log.error("{}exception=>{},\n,{}", customDB, existAreas, e2);
        }
    }

    @Override
    public void readAndCalculateStress(String primaryDB, String customDB) {
        DynamicDataSourceContextHolder.setDataSource(primaryDB);
        List<Stress> listStress = (List<Stress>) mapCache.get("STRESS" + customDB);
        log.info("STRESS COUNT=>{}", listStress.size());
        if (CollectionUtils.isEmpty(listStress)) {
            return;
        }
        List<List<Stress>> result = AVGList.averageAssign(listStress, 1000);
        final int RUNNER_COUNT = result.size();
        for (int i = 0; i < RUNNER_COUNT; i++) {
            try {
                DynamicDataSourceContextHolder.setDataSource(customDB);
                dataUtil.optionStress(customDB, result.get(i), (List<String>) mapCache.get("POINT" + customDB));
                writeToHiMine(primaryDB, customDB, result.get(i));
                writeToCurMine(primaryDB, customDB, result.get(i));
            } catch (Exception e) {
                log.error("STRESS WRITER EXCEPTION=>{},{}", customDB, e);
            } finally {
                DynamicDataSourceContextHolder.setDataSource(primaryDB);
                stressMapper.deleteGtoupData(result.get(i));// 删除，正式使用时释放
            }
        }
        try {
            DynamicDataSourceContextHolder.setDataSource(customDB);
            List<StressDataInfo> stressDataInfos = stressDataInfoMapper.getDataInfoCache();
            if (stressDataInfos.size() != 0) {
                // STRESS_TOP_DATA_INFO
                dataUtil.writeToTopByPoints(stressDataInfos, stressMeasurePointMapper.getAllPoint(), customDB);
            }
        } catch (Exception e) {
            log.info("STRESS TOP WRITER EXCEPTION=>{},{}", customDB, e);
        }
        mapCache.put("CURMINE" + customDB, curMineInfoMapper.getAllCurMine());
        log.info("矿区{}应力处理完毕", customDB);
        DynamicDataSourceContextHolder.restoreDataSource();
//		}
    }

    @Override
    public void readAndCalculateQuake(String primaryDB, String customDB) {
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<Area> areas = areaMapper.getArea();
        DynamicDataSourceContextHolder.setDataSource(primaryDB);
        List<List<Quake>> result = AVGList.averageAssign((List<Quake>) mapCache.get("QUAKE" + customDB), 200);
//		List<Area> areas = (List<Area>) mapCache.get("AREA" + customDB);
        final int RUNNER_COUNT = result.size();
        for (int i = 0; i < RUNNER_COUNT; i++) {
            try {
                dataUtil.optionQuake(areas, primaryDB, customDB, result.get(i));
            } catch (Exception e) {
                log.error("微震写入异常=>{}", e);
            } finally {
                DynamicDataSourceContextHolder.setDataSource(primaryDB);
                quakeMapper.deleteGtoupData(result.get(i));
            }
        }
    }

    @Override
    public void writeToCurMine(String primaryDB, String customDB, List<Stress> listStress) {
        dataUtil.optionCurMine(customDB, listStress, (List<StressMeasurePoint>) mapCache.get("POINT" + customDB));
    }

    @Override
    public void writeToHiMine(String primaryDB, String customDB, List<Stress> stresses) {
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<Area> areas = areaMapper.getArea();
        List<Stress> warnStress = new ArrayList<>();
        for (Stress stressTemp : stresses) {
            if (stressTemp.getValue() >= 9) {
                warnStress.add(stressTemp);
            }
        }
        List<StressMeasurePoint> stressMeasurePoints = stressMeasurePointMapper.getAllPoint();
        dataUtil.optionHiMine(customDB, warnStress, stressMeasurePoints, areas);
    }

    @Override
    public void updatePointTime(String primaryDB, String customDB) {
        try {
            DynamicDataSourceContextHolder.setDataSource(customDB);
            List<StressMeasurePoint> listUPPoints = new ArrayList<>();
            List<StressDataInfo> stressDataInfos = (List<StressDataInfo>) mapCache.get("STRDATA" + customDB);
            List<StressMeasurePoint> stressMeasurePoints = stressMeasurePointMapper.getAllPoint();
            for (int i = 0; i < stressMeasurePoints.size(); i++) {
                StressMeasurePoint stressMeasurePoint = stressMeasurePoints.get(i);
                try {
                    DynamicDataSourceContextHolder.setDataSource(customDB);
                    List<StressDataInfo> stressDataInfosList = stressDataInfos.stream()
                            .filter(e -> e.getMpId() == (stressMeasurePoint.getId())).collect(Collectors.toList());
                    Collections.sort(stressDataInfosList, Comparator.comparing(StressDataInfo::getAcquisitionTime));
                    Collections.reverse(stressDataInfosList);
                    List<Date> listTime = stressDataInfosList.stream().map(StressDataInfo::getAcquisitionTime)
                            .collect(Collectors.toList());
                    if (listTime.size() != 0) {
                        stressMeasurePoint.setFromTime(listTime.get(listTime.size() - 1));
                        stressMeasurePoint.setToTime(listTime.get(0));
                        listUPPoints.add(stressMeasurePoint);
                    }
                } catch (Exception e) {
                    log.info("UPDATE MEASUREPOINT TIME EXCEPTION=>{},{}", customDB, e);
                } finally {
                }
            }
            if (listUPPoints.size() != 0) {
                stressMeasurePointMapper.updateGroupData(listUPPoints);
            }
            DynamicDataSourceContextHolder.restoreDataSource();
        } catch (Exception e) {
            log.info("{}  EXCEPTION=>{}", customDB, e);
        }
    }

    @Override
    public void writeToMeasurePoint(String primaryDB, String customDB) {
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<Area> areas = areaMapper.getArea();
        List<StressMeasurePoint> stressMeasurePoints = stressMeasurePointMapper.getAllPoint();
        dataUtil.getMeasurPointId(stressMeasurePoints, areas, (List<Stress>) mapCache.get("STRESS" + customDB),
                primaryDB, customDB, (List<PointSign>) mapCache.get("PSIGN" + customDB));
        DynamicDataSourceContextHolder.setDataSource(customDB);
        mapCache.put("POINT" + customDB, stressMeasurePointMapper.getAllPoint());
    }

    @Override
    public void writeToPlatform(String primaryDB, String customDB) {
        List<Mine> mines = ((List<Mine>) mapCache.get("MINE" + customDB));
        DynamicDataSourceContextHolder.setDataSource(customDB);
        List<ConnStatus> listConStatus = new ArrayList<>();
        List<ConnTopStatus> listINTopStatus = new ArrayList<>();
        List<ConnTopStatus> listUPTopStatus = new ArrayList<>();
        hasSomeWrong:
        for (Mine mine : mines) {
            ConnStatus stressConnStatu = dataUtil.getStressConStatus(customDB,
                    (List<CurMineInfo>) mapCache.get("CURMINE" + customDB), mine);
            ConnStatus quakeConnStatu = dataUtil.getQuakeConStatus(customDB,
                    (List<CurMineInfo>) mapCache.get("CURMINE" + customDB), mine);
            if (stressConnStatu != null) {
                listConStatus.add(stressConnStatu);
                // 把单个状态信息与Top表对比
                ConnTopStatus stresssConnTopStatus = new ConnTopStatus();
                try {
                    CopyUtils.Copy(stressConnStatu, stresssConnTopStatus);
                    List<ConnTopStatus> FilterList = ((List<ConnTopStatus>) mapCache.get("CONNTOPSTATUS" + customDB))
                            .stream().filter(e -> e.getMineCode().equals(stresssConnTopStatus.getMineCode()))
                            .collect(Collectors.toList());
                    if (FilterList.size() == 0) {
                        listINTopStatus.add(stresssConnTopStatus);
                    } else {
                        listUPTopStatus.add(stresssConnTopStatus);
                    }
                } catch (Exception e) {
                    log.info("连接状态写入异常=>{}", e);
                }
            } else {
                continue hasSomeWrong;
            }
            if (quakeConnStatu != null) {
                listConStatus.add(quakeConnStatu);
                // 把单个状态信息与Top表对比
                ConnTopStatus quakeConnTopStatus = new ConnTopStatus();
                try {
                    CopyUtils.Copy(quakeConnStatu, quakeConnTopStatus);
                    List<String> listCondition = new ArrayList<>();
                    listCondition.add(quakeConnTopStatus.getMineCode());
                    List<ConnTopStatus> FilterList = ((List<ConnTopStatus>) mapCache.get("CONNTOPSTATUS" + customDB))
                            .stream().filter((ConnTopStatus c) -> listCondition.contains(c.getMineCode()))
                            .collect(Collectors.toList());
                    FilterList = FilterList.stream().filter(e -> e.getType().equals(quakeConnTopStatus.getType()))
                            .collect(Collectors.toList());
                    if (FilterList.size() == 0) {
                        listINTopStatus.add(quakeConnTopStatus);
                    } else {
                        listUPTopStatus.add(quakeConnTopStatus);
                    }
                } catch (Exception e) {
                    log.info("连接状态写入异常=>{}", e);
                }
            } else {
                continue hasSomeWrong;
            }
        }
        log.info("FENGYUANSHIYE=>{}", listUPTopStatus);
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
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public boolean hasNeedAnalysisData() {
        return stressMapper.stressCount() != 0 || quakeMapper.quakeCount() != 0;
    }
}
