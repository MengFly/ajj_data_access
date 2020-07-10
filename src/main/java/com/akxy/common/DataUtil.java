package com.akxy.common;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.*;
import com.akxy.util.AreaUtil;
import com.akxy.util.ParseUtil;
import com.akxy.util.StressMpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 数据操作工具类
 *
 * @author cqz 2019-3-11
 */
@Slf4j
@Component
public class DataUtil {

    private static final String DEFAULT_TUNNEL_NAME = "巷道";

    @Autowired
    private StressMeasurePointMapper stressMeasurePointMapper;
    @Autowired
    private StressTopDataInfoMapper stressTopDataInfoMapper;
    @Autowired
    private OrganMineMapper organMineMapper;
    @Autowired
    private AreaTopDataInfoMapper areaTopDataInfoMapper;
    @Autowired
    private AreaMapper areaMapper;


    /**
     * 组装没有录入在子矿区数据库中的测点信息
     */
    public List<StressMeasurePoint> optionPoint(List<Stress> stresses, String customDb) {
        // 所有没有被记录在子矿区数据库中的测点信息
        List<StressMeasurePoint> returnPoints = new ArrayList<>();
        // 测点标识，用于检查测点是否已经处理过了
        Set<String> pointSignSet = new HashSet<>();
        try (AreaUtil areaUtil = new AreaUtil()) {
            for (Stress stress : stresses) {
                // 防止巷道名称为空
                stress.setTunnelname(ParseUtil.getOrDefault(stress.getTunnelname(), DEFAULT_TUNNEL_NAME));
                String pointSign = getPointSign(stress);
                // 已经处理过的测点不处理
                if (pointSignSet.contains(pointSign)) {
                    continue;
                }
                // 已经存在过的测点不处理
                Long mpAreaId = areaUtil.getId(stress.getAreaname(), customDb, areaMapper);
                boolean mpIsExists = stressMeasurePointMapper
                        .countBy(mpAreaId, stress.getTunnelname(), stress.getDistance(), stress.getDepth()) > 0;
                if (!mpIsExists) {
                    pointSignSet.add(pointSign);
                    StressMeasurePoint stressMeasurePoint = getStressMeasurePoint(stress, mpAreaId);
                    returnPoints.add(stressMeasurePoint);
                }
                pointSignSet.add(pointSign);
            }
        }

        return returnPoints;
    }

    /**
     * 获得测点的标识符，工作面名称 + 巷道名称 + Distance + Depth
     *
     * @param stress 应力信息
     * @return 测点标识符
     */
    public String getPointSign(Stress stress) {
        return stress.getAreaname() + stress.getTunnelname() + stress.getDistance() + stress.getDepth();
    }

    public static StressMeasurePoint getStressMeasurePoint(Stress stress, Long mpAreaId) {
        StressMeasurePoint stressMeasurePoint = new StressMeasurePoint();
        stressMeasurePoint.setAreaId(mpAreaId);
        stressMeasurePoint.setName(stress.getName());
        stressMeasurePoint.setTunnelName(stress.getTunnelname());
        stressMeasurePoint.setX(stress.getX());
        stressMeasurePoint.setY(stress.getY());
        stressMeasurePoint.setZ(stress.getZ());
        stressMeasurePoint.setDepth(stress.getDepth());
        stressMeasurePoint.setDistance(stress.getDistance());
        stressMeasurePoint.setInitialValue(stress.getInitialvalue());
        stressMeasurePoint.setYellowWarnvalue(stress.getYellowwarn());
        stressMeasurePoint.setRedWarnvalue(stress.getRedwarn());
        if (StringUtils.isEmpty(stress.getMemo())) {
            stressMeasurePoint.setMemo("0");
        } else {
            stressMeasurePoint.setMemo(stress.getMemo());
        }
        Date time = new Date();
        stressMeasurePoint.setFromTime(time);
        stressMeasurePoint.setToTime(time);
        return stressMeasurePoint;
    }

    /**
     * 组装StressDataInfo信息
     *
     * @param stress 经过处理的中间库Stress信息
     * @return List<StressDataInfo> 组装完成的StressDataInfo信息
     */
    public StressDataInfo getStressDataInfo(String customDb, Long areaId, Stress stress) {
        StressDataInfo stressDataInfo = new StressDataInfo();
        try {
            stressDataInfo.setAcquisitionTime(stress.getCollectiontime());
            // 结果集
            stressDataInfo.setAreaId(areaId);
            stressDataInfo.setWarnStatus(getWarnStatus(stress.getWarnrecord()));
            stressDataInfo.setDepth(BigDecimal.valueOf(stress.getDepth()));
            stressDataInfo.setDistance(BigDecimal.valueOf(stress.getDistance()));
            stressDataInfo.setMemo("0");
            stressDataInfo.setMpName(stress.getName());
            stressDataInfo.setPValue(stress.getValue());
            stressDataInfo.setX(stress.getX());
            stressDataInfo.setY(stress.getY());
            stressDataInfo.setZ(stress.getZ());
            Double yellowwarn = stress.getYellowwarn();
            if (yellowwarn != null) {
                if (yellowwarn >= 100) {
                    stressDataInfo.setYellowValue(BigDecimal.valueOf(99));
                } else {
                    stressDataInfo.setYellowValue(BigDecimal.valueOf(ParseUtil.numberDigist(yellowwarn, 2)));
                }
            } else {
                stressDataInfo.setYellowValue(BigDecimal.ZERO);
            }
            Double redwarn = stress.getRedwarn();
            if (redwarn != null) {
                if (redwarn >= 100) {
                    stressDataInfo.setRedValue(BigDecimal.valueOf(99));
                } else {
                    stressDataInfo.setRedValue(BigDecimal.valueOf(ParseUtil.numberDigist(redwarn, 2)));
                }
            } else {
                stressDataInfo.setRedValue(BigDecimal.ZERO);
            }
            if (stress.getInitialvalue() == 0) {
                stressDataInfo.setZfIndex(0.);
            } else {
                double zf = (stress.getValue() - stress.getInitialvalue()) / stress.getInitialvalue();
                stressDataInfo.setZfIndex(ParseUtil.numberDigist(zf, 4));
            }
        } catch (Exception e) {
            log.error("处理应力错误{}", e.getMessage());
            return null;
        }
        return stressDataInfo;
    }


    private short getWarnStatus(String warnRecord) {
        if (warnRecord == null) {
            return 0;
        }
        if ("黄色".equals(warnRecord)) {
            return 1;
        }
        if ("红色".equals(warnRecord)) {
            return 2;
        }
        return 0;
    }

    /**
     * 组装POSResult数据
     */
    public synchronized PosResult assemblePosResult(Long areaId, Quake quake, String quakeWarnConfig) {
        PosResult posResult = new PosResult();

        posResult.setCollectTime(quake.getCollectiontime());
        posResult.setX(ParseUtil.getFValue(quake.getX(), Double::parseDouble));
        posResult.setY(ParseUtil.getFValue(quake.getY(), Double::parseDouble));
        posResult.setZ(ParseUtil.getFValue(quake.getZ(), Double::parseDouble));
        posResult.setEnergy(ParseUtil.getFValue(quake.getEnergy(), Double::parseDouble));
        posResult.setAreaId(areaId);
        posResult.setSource(getQuakeWarn(quake.getMinecode(), quake.getEnergy(), quakeWarnConfig));
        posResult.setMemo("0");
        posResult.setAvgswing(ParseUtil.getIValue(quake.getAvgswing(), Long::parseLong));
        posResult.setMaxswing(ParseUtil.getIValue(quake.getMaxswing(), Long::parseLong));
        posResult.setBasicfreq(ParseUtil.getIValue(quake.getBasicfreq(), Long::parseLong));
        posResult.setChcount(ParseUtil.getIValue(quake.getChcount(), Long::parseLong));
        posResult.setExcstatus(quake.getExcstatus());
        posResult.setInstallway(quake.getInstallway());
        posResult.setPosdesc(quake.getPosdesc());
        posResult.setSampfreq(ParseUtil.getIValue(quake.getSampfreq(), Long::parseLong));
        posResult.setSamplength(ParseUtil.getIValue(quake.getSamplength(), Long::parseLong));
        posResult.setSensitivity(ParseUtil.getIValue(quake.getSensitivity(), Long::parseLong));
        posResult.setSensordir(ParseUtil.getOrDefault(quake.getSensordir(), "").toLowerCase());
        posResult.setSensortype(quake.getSensortype());
        posResult.setTrigch(quake.getTrigch());
        return posResult;
    }


    public List<StressDataInfo> optionAllStress(String customDb, List<Stress> listStress) {
        List<StressDataInfo> listStressDataInfos = new ArrayList<>();
        DynamicDataSourceContextHolder.setDataSource(customDb);
        try (AreaUtil areaUtil = new AreaUtil();
             StressMpUtil mpUtil = new StressMpUtil()) {
            listStress.stream().filter(Objects::nonNull).forEach(stress -> {
                Long areaId = areaUtil.getId(stress.getAreaname(), customDb, areaMapper);
                StressDataInfo stressDataInfo = getStressDataInfo(customDb, areaId, stress);
                if (stressDataInfo != null) {
                    Long stressMpId = mpUtil.getId(areaId, stress.getTunnelname(), stress.getDistance(),
                            stress.getDepth(), customDb, stressMeasurePointMapper);
                    stressDataInfo.setMpId(ParseUtil.getOrDefault(stressMpId, 0L));
                    listStressDataInfos.add(stressDataInfo);
                }
            });
        }

        return listStressDataInfos;
    }

    /**
     * 遍历所有测点的最新数据，并写入STRESS_TOP_DATAINFO表中
     */
    public void writeToTopByPoints(String customDb, List<StressDataInfo> stressDataInfos) {
        Map<String, StressTopDataInfo> needInsertTopInfos = new HashMap<>(32);
        Map<String, StressTopDataInfo> needUpdateTopInfos = new HashMap<>(32);
        DynamicDataSourceContextHolder.setDataSource(customDb);
        for (StressDataInfo stressDataInfo : stressDataInfos) {
            StressTopDataInfo stressTopDataInfo = new StressTopDataInfo();
            BeanUtils.copyProperties(stressDataInfo, stressTopDataInfo);
            String key = stressTopDataInfo.getAreaId() + "" + stressTopDataInfo.getMpId();

            // 判断需要更新列表里面是否存在了当前工作面和测点的应力信息，存在则取最新时间的
            StressTopDataInfo insertInfo = needInsertTopInfos.getOrDefault(key, null);
            if (insertInfo != null) {
                if (stressDataInfo.getAcquisitionTime().getTime() > insertInfo.getAcquisitionTime().getTime()) {
                    needInsertTopInfos.put(key, stressTopDataInfo);
                }
                continue;
            }

            StressTopDataInfo updateInfo = needUpdateTopInfos.getOrDefault(key, null);
            if (updateInfo != null) {
                if (stressDataInfo.getAcquisitionTime().getTime() > updateInfo.getAcquisitionTime().getTime()) {
                    needUpdateTopInfos.put(key, stressTopDataInfo);
                }
                continue;
            }

            int count = stressTopDataInfoMapper.countBy(stressDataInfo.getAreaId(), stressDataInfo.getMpId());

            if (count > 0) {
                needUpdateTopInfos.put(key, stressTopDataInfo);
            } else {
                needInsertTopInfos.put(key, stressTopDataInfo);
            }
        }

        DynamicDataSourceContextHolder.setDataSource(customDb);
        if (!needInsertTopInfos.isEmpty()) {
            int insertCount = stressTopDataInfoMapper.insertGroupData(new ArrayList<>(needInsertTopInfos.values()));
            log.info(">> 应力Top需新增({})条，成功新增({})条", needInsertTopInfos.size(), insertCount);
        }
        if (!needUpdateTopInfos.isEmpty()) {
            stressTopDataInfoMapper.updateTopData(new ArrayList<>(needUpdateTopInfos.values()));
            log.info(">> 应力Top更新({}) 条", needUpdateTopInfos.size());
        }

    }


    /**
     * 组装历史预警数据
     */
    public MineInfo getHiMineInfo(StressDataInfo stress) {
        MineInfo hiMineInfo = new MineInfo();
        hiMineInfo.setAreaId(stress.getAreaId());
        hiMineInfo.setType((short) 1);
        hiMineInfo.setAcquisitionTime(stress.getAcquisitionTime());
        hiMineInfo.setStressValue(stress.getPValue());
        hiMineInfo.setQuakeValue((double) 0);
        hiMineInfo.setMemo(stress.getMemo());
        hiMineInfo.setMpId(stress.getMpId());
        return hiMineInfo;
    }

    /**
     * 获取微震预警等级
     */
    public String getQuakeWarn(String customDb, Double energy, String quakeWarnConfig) {
        String warnStatus;
        DynamicDataSourceContextHolder.setDataSource(customDb);
        String[] temp = quakeWarnConfig.split(",");
        if (energy < Integer.parseInt(temp[0])) {
            warnStatus = "0";
        } else if (energy > Integer.parseInt(temp[0]) && energy < Integer.parseInt(temp[1])) {
            warnStatus = "1";
        } else {
            warnStatus = "2";
        }
        return warnStatus;
    }

    public ConnStatus getStressConStatus(String customDb, String mineName, Date stressTopNewDate, int topNewDate) {
        ConnStatus connStatus = getConnStatus(customDb, mineName, stressTopNewDate, topNewDate);
        if (connStatus == null) {
            return null;
        }
        connStatus.setType("7");
        return connStatus;
    }

    public ConnStatus getQuakeConStatus(String customDb, String mineName, Date quakeTopNewDate, int quakeTopTimeOut) {
        ConnStatus connStatus = getConnStatus(customDb, mineName, quakeTopNewDate, quakeTopTimeOut);
        if (connStatus == null) {
            return null;
        }
        connStatus.setType("6");
        return connStatus;
    }

    public ConnStatus getConnStatus(String customDb, String mineName, Date stressTopNewDate, int topNewDate) {
        ConnStatus connStatus = new ConnStatus();
        DynamicDataSourceContextHolder.setDataSource(customDb);
        Short warnValue = areaTopDataInfoMapper.findAreaValue();
        DynamicDataSourceContextHolder.setDataSource("1000");
        connStatus.setMineCode(customDb);
        connStatus.setMineName(mineName);
        int deviationStress;
        if (stressTopNewDate != null) {
            deviationStress = (int) ((System.currentTimeMillis() - stressTopNewDate.getTime()) / (1000 * 60));
            if (deviationStress > topNewDate) {
                connStatus.setConnStatus("0");
            } else {
                connStatus.setConnStatus("1");
            }
        } else {
            connStatus.setConnStatus("0");
        }

        if (warnValue == null) {
            connStatus.setWarningDetail("无数据");
        } else {
            connStatus.setWarningDetail(String.valueOf(warnValue));
        }
        connStatus.setAcquireTime(stressTopNewDate);
        connStatus.setMemo("连接状态");
        return connStatus;
    }


}


