package com.akxy.common;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.AreaTopDataInfoMapper;
import com.akxy.mapper.OrganMineMapper;
import com.akxy.mapper.StressMeasurePointMapper;
import com.akxy.mapper.StressTopDataInfoMapper;
import com.akxy.service.impl.LocalCacheServiceImpl;
import com.akxy.util.AreaUtil;
import com.akxy.util.ParseUtil;
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

    @Autowired
    private StressMeasurePointMapper stressMeasurePointMapper;
    @Autowired
    private StressTopDataInfoMapper stressTopDataInfoMapper;
    @Autowired
    private OrganMineMapper organMineMapper;
    @Autowired
    private AreaTopDataInfoMapper areaTopDataInfoMapper;
    @Autowired
    private LocalCacheServiceImpl localCacheService;


    /**
     * 组装没有录入在子矿区数据库中的测点信息
     */
    public List<StressMeasurePoint> optionPoint(List<StressMeasurePoint> exitsStressMp,
                                                List<Stress> stresses, String customDb) {
        // 所有没有被记录在子矿区数据库中的测点信息
        List<StressMeasurePoint> returnPoints = new ArrayList<>();
        Map<String, Area> areas = localCacheService.getMineAreaCache(customDb);
        // 测点标识，用于检查测点是否已经处理过了
        Set<String> signList = new HashSet<>();
        for (Stress stress : stresses) {
            String sign = stress.getTunnelname() + stress.getDepth() + stress.getDistance();
            // 已经处理过的测点不处理
            if (signList.contains(sign)) {
                continue;
            }
            // 已经存在过的测点不处理
            boolean mpIsExists = exitsStressMp.stream().anyMatch(
                    point -> (point.getTunnelName() + point.getDepth() + point.getDistance()).equals(sign));
            if (mpIsExists) {
                signList.add(sign);
                continue;
            }

            // 剩下的肯定就是既没有处理过的，也没有存在于子矿区数据库中的测点信息
            signList.add(sign);
            Area area = areas.get(stress.getAreaname());
            Long mpAreaId = area == null ? AreaUtil.getAllMineId(customDb) : area.getId();
            StressMeasurePoint stressMeasurePoint = getStressMeasurePoint(stress, mpAreaId);
            returnPoints.add(stressMeasurePoint);
        }
        return returnPoints;
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
    public StressDataInfo getStressDataInfo(String customDb, Map<String, Area> areas, Stress stress) {
        StressDataInfo stressDataInfo = new StressDataInfo();
        try {
            stressDataInfo.setAcquisitionTime(stress.getCollectiontime());
            // 结果集
            Area area = areas.get(stress.getAreaname());
            Long areaId = area == null ?
                    areas.values().stream().findFirst().map(Area::getId).orElse(AreaUtil.getAllMineId(customDb)) : area.getId();
            stressDataInfo.setAreaId(areaId);
            stressDataInfo.setWarnStatus(stress.getWarnrecord() == null ? (short) 0
                    : ("正常".equals(stress.getWarnrecord()) ? (short) 0
                    : ("黄色".equals(stress.getWarnrecord()) ? (short) 1 : (short) 2)));
            stressDataInfo.setDepth(BigDecimal.valueOf(stress.getDepth()));
            stressDataInfo.setDistance(BigDecimal.valueOf(stress.getDistance()));
            stressDataInfo.setMemo("0");
            stressDataInfo.setMpName(stress.getName());
            stressDataInfo.setpValue(stress.getValue());
            stressDataInfo.setX(stress.getX());
            stressDataInfo.setY(stress.getY());
            stressDataInfo.setZ(stress.getZ());
            stressDataInfo.setYellowValue(BigDecimal.valueOf(stress.getYellowwarn()));
            stressDataInfo.setRedValue(BigDecimal.valueOf(stress.getRedwarn()));
            stressDataInfo.setWarnStatus(getWarnStatus(stress.getValue()));
            if (stress.getInitialvalue() == 0) {
                stressDataInfo.setZfIndex(0.);
            } else {
                stressDataInfo.setZfIndex((stress.getValue() - stress.getInitialvalue()) / stress.getInitialvalue());
            }
        } catch (Exception e) {
            log.info("{},{} EXCEPTION=>{}", customDb, stress, e);
            return null;
        }
        return stressDataInfo;
    }

    /**
     * 组装POSResult数据
     */
    public PosResult assemblePosResult(Long areaId, Quake quake, String quakeWarnConfig) {
        PosResult posResult = new PosResult();

        posResult.setCollectTime(quake.getCollectiontime());
        posResult.setX(quake.getX());
        posResult.setY(quake.getY());
        posResult.setZ(quake.getZ());
        posResult.setEnergy(quake.getEnergy());
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
        Map<String, Area> areas = localCacheService.getMineAreaCache(customDb);
        List<StressMeasurePoint> stressMeasurePoints = localCacheService.getMinePointCache(customDb);
        DynamicDataSourceContextHolder.setDataSource(customDb);
        listStress.stream().filter(Objects::nonNull).forEach(stress -> {
            StressDataInfo stressDataInfo = getStressDataInfo(customDb, areas, stress);
            if (stressDataInfo != null) {
                StressMeasurePoint stressMeasurePoint = stressMeasurePoints.stream()
                        .filter(point -> Objects.equals(point.getTunnelName(), stress.getTunnelname()) &&
                                Objects.equals(point.getDepth(), stress.getDepth()) &&
                                Objects.equals(point.getDistance(), stress.getDepth()))
                        .findFirst().orElse(null);
                Long stressMpId;
                if (stressMeasurePoint == null) {
                    stressMpId = stressMeasurePointMapper.getMPID(stress.getTunnelname(),
                            stress.getDepth(), stress.getDistance());
                } else {
                    stressMpId = stressMeasurePoint.getId();
                }
                stressDataInfo.setMpId(stressMpId);
                listStressDataInfos.add(stressDataInfo);
            }
        });
        return listStressDataInfos;
    }

    /**
     * 遍历所有测点的最新数据，并写入STRESS_TOP_DATAINFO表中
     */
    public void writeToTopByPoints(String customDb, String mineName, List<StressDataInfo> stressDataInfos) {
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
            log.info(">> [{}-{}] 应力Top需新增({})条，成功新增({})条", customDb, mineName, needInsertTopInfos.size(), insertCount);
        }
        if (!needUpdateTopInfos.isEmpty()) {
            needUpdateTopInfos.values().forEach(stressTopDataInfo ->
                    stressTopDataInfoMapper.updateTopData(stressTopDataInfo));
            log.info(">> [{}-{}] 应力Top更新({}) 条", customDb, mineName, needUpdateTopInfos.size());
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
        hiMineInfo.setStressValue(stress.getpValue());
        hiMineInfo.setQuakeValue((double) 0);
        hiMineInfo.setMemo(stress.getMemo());
        hiMineInfo.setMpId(stress.getMpId());
        return hiMineInfo;
    }

    /**
     * 获取Stress预警状态
     */
    public Short getWarnStatus(Double pValue) {
        short warnStatus;
        if (pValue < 9) {
            warnStatus = 0;
        } else if (pValue > 9 && pValue < 11) {
            warnStatus = 1;
        } else {
            warnStatus = 2;
        }
        return warnStatus;
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

    public ConnStatus getStressConStatus(String customDb, Mine mine, Date stressTopNewDate, int topNewDate) {
        ConnStatus connStatus = getConnStatus(customDb, mine, stressTopNewDate, topNewDate);
        if (connStatus == null) {
            return null;
        }
        connStatus.setType("7");
        return connStatus;
    }

    public ConnStatus getQuakeConStatus(String customDb, Mine mine, Date quakeTopNewDate, int quakeTopTimeOut) {
        ConnStatus connStatus = getConnStatus(customDb, mine, quakeTopNewDate, quakeTopTimeOut);
        if (connStatus == null) {
            return null;
        }
        connStatus.setType("6");
        return connStatus;
    }

    public ConnStatus getConnStatus(String customDb, Mine mine, Date stressTopNewDate, int topNewDate) {
        ConnStatus connStatus = new ConnStatus();
        DynamicDataSourceContextHolder.setDataSource(customDb);
        Short warnValue = areaTopDataInfoMapper.findAreaValue();
        DynamicDataSourceContextHolder.setDataSource("1000");
        String idAndName = organMineMapper.findIDByName(mine.getName() + "%");
        if (idAndName == null) {
            return null;
        } else {
            connStatus.setMineCode(idAndName.substring(0, idAndName.indexOf(",")));
        }
        connStatus.setMineName(idAndName.substring(idAndName.indexOf(",") + 1));
        Date curDate = new Date();
        int deviationStress;
        if (stressTopNewDate != null) {
            deviationStress = (int) ((curDate.getTime() - stressTopNewDate.getTime()) / (1000 * 60));
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
