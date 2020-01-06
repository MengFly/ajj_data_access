package com.akxy.common;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.AreaTopDataInfoMapper;
import com.akxy.mapper.OrganMineMapper;
import com.akxy.mapper.StressMeasurePointMapper;
import com.akxy.mapper.StressTopDataInfoMapper;
import com.akxy.util.AreaUtil;
import com.akxy.util.CopyUtils;
import com.akxy.util.ParseUtil;
import lombok.extern.slf4j.Slf4j;
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


    /**
     * 组装没有录入在子矿区数据库中的测点信息
     */
    public static List<StressMeasurePoint> optionPoint(List<StressMeasurePoint> exitsStressMP, List<Area> listArea,
                                                       List<Stress> stresses, String customDB) {
        // 所有没有被记录在子矿区数据库中的测点信息
        List<StressMeasurePoint> returnPoints = new ArrayList<>();

        Set<String> signList = new HashSet<>();// 测点标识，用于检查测点是否已经处理过了
        for (Stress stress : stresses) {
            String sign = stress.getTunnelname() + stress.getDepth() + stress.getDistance();
            // 已经处理过的测点不处理
            if (signList.contains(sign)) {
                continue;
            }
            // 已经存在过的测点不处理
            boolean mpIsExists = exitsStressMP.stream().anyMatch(
                    point -> (point.getTunnelName() + point.getDepth() + point.getDistance()).equals(sign));
            if (mpIsExists) {
                signList.add(sign);
                continue;
            }

            // 剩下的肯定就是既没有处理过的，也没有存在于子矿区数据库中的测点信息
            signList.add(sign);
            Long mpAreaId = listArea.stream()
                    .filter(area -> area.getName().equals(stress.getAreaname()))
                    .findFirst().map(Area::getId).orElse(AreaUtil.getAllMineId(customDB));
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
     * @param areas
     * @param stress 经过处理的中间库Stress信息
     * @return List<StressDataInfo> 组装完成的StressDataInfo信息
     */
    public StressDataInfo getStressDataInfo(String customDB, List<Area> areas, Stress stress) {
        StressDataInfo stressDataInfo = new StressDataInfo();
        try {
            stressDataInfo.setAcquisitionTime(stress.getCollectiontime());
            // 结果集
            Long areaId = areas.stream().filter(e -> e.getName().equals(stress.getAreaname()))
                    .findFirst().map(Area::getId).orElse(areas.get(0).getId());
            stressDataInfo.setAreaId(areaId);
            stressDataInfo.setWarnStatus(stress.getWarnrecord() == null ? (short) 0
                    : (stress.getWarnrecord().equals("正常") ? (short) 0
                    : (stress.getWarnrecord().equals("黄色") ? (short) 1 : (short) 2)));
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
            log.info("{},{} EXCEPTION=>{}", customDB, stress, e);
            return null;
        }
        return stressDataInfo;
    }

    /**
     * 组装POSResult数据
     */
    public PosResult assemblePosResult(List<Area> areas, String customDB, Quake quake, String quakeWarnConfig) {
        PosResult posResult = new PosResult();

        posResult.setCollectTime(quake.getCollectiontime());
        posResult.setX(quake.getX());
        posResult.setY(quake.getY());
        posResult.setZ(quake.getZ());
        posResult.setEnergy(quake.getEnergy());

        Long areaId = areas.stream().filter(a -> a.getName().equals(quake.getAreaname()))
                .findFirst().map(Area::getId).orElse(AreaUtil.getAllMineId(customDB));
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

    /**
     * 遍历Stress集合 将数据写入STRESS_DATAINFO表
     */
    public List<StressDataInfo> optionStress(String customDB, List<Area> areas, List<Stress> listStress, List<StressMeasurePoint> stressMeasurePoints) {
        List<StressDataInfo> listStressDataInfos = new ArrayList<>();
        DynamicDataSourceContextHolder.setDataSource(customDB);
        // 空值和应力值大于30的不处理
        listStress.stream().filter(stress -> stress != null && stress.getValue() <= 30).forEach(stress -> {
            StressDataInfo stressDataInfo = getStressDataInfo(customDB, areas, stress);
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
    public void writeToTopByPoints(List<StressDataInfo> stressDataInfos, List<StressTopDataInfo> stressTopDataInfos,
                                   List<StressMeasurePoint> stressMeasurePoints,
                                   String customDB) {
        List<StressTopDataInfo> insertTopStress = new ArrayList<>();
        for (StressMeasurePoint stressMeasurePoint : stressMeasurePoints) {
            Optional<StressDataInfo> mpStress = stressDataInfos.stream()
                    .filter(stressDataInfo -> stressDataInfo.getAreaId().equals(stressMeasurePoint.getAreaId())
                            && stressDataInfo.getMpId().equals(stressMeasurePoint.getId()))
                    .findFirst();
            mpStress.ifPresent(stressDataInfo -> {
                StressTopDataInfo stressTopDataInfo = new StressTopDataInfo();
                try {
                    CopyUtils.Copy(stressDataInfo, stressTopDataInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int temp = (int) stressTopDataInfos.stream()
                        .filter(topDataInfo ->
                                topDataInfo.getAreaId().equals(stressDataInfo.getAreaId())
                                        && topDataInfo.getMpId().equals(stressDataInfo.getMpId()))
                        .count();
                if (temp >= 1) {
                    stressTopDataInfoMapper.updateTopData(stressTopDataInfo);
                } else {
                    insertTopStress.add(stressTopDataInfo);
                }
            });

        }
        try {
            if (insertTopStress.size() != 0) {
                DynamicDataSourceContextHolder.setDataSource(customDB);
                stressTopDataInfoMapper.insertGroupData(insertTopStress);
            }
        } catch (Exception e) {
            log.error("TOP INSERT ERROR=>{},{}", customDB, e);
        }
    }

    /**
     * 组装实时预警信息
     */
    public CurMineInfo getCurMineInfo(String customDB, List<Area> areas, List<StressMeasurePoint> measurePoints, Stress stress) {
        DynamicDataSourceContextHolder.setDataSource(customDB);
        Long areaId = areas.stream().filter(area -> Objects.equals(area.getName(), stress.getAreaname()))
                .findFirst().map(Area::getId).orElse(AreaUtil.getAllMineId(customDB));
        CurMineInfo curMineInfo = new CurMineInfo();
        curMineInfo.setAreaId(areaId);
        curMineInfo.setType((short) 1); // 0 微震 1应力
        curMineInfo.setAcquisitionTime(stress.getCollectiontime());
        curMineInfo.setStressValue(stress.getValue());
        curMineInfo.setQuakeValue((double) 0);
        curMineInfo.setMemo(stress.getMemo());
        Optional<StressMeasurePoint> stressMeasurePoint = measurePoints.stream()
                .filter(
                        stressMp -> Objects.equals(stressMp.getTunnelName(), stress.getTunnelname()) &&
                                Objects.equals(stressMp.getDepth(), stress.getDepth()) &&
                                Objects.equals(stressMp.getDistance(), stress.getDistance()))
                .findFirst();
        stressMeasurePoint.ifPresent(mp -> curMineInfo.setMpId(mp.getId()));
        return curMineInfo;
    }


    /**
     * 组装历史预警数据
     */
    public HiMineInfo getHiMineInfo(String customDB, Stress stress, List<StressMeasurePoint> liStressMeasurePoints,
                                    List<Area> listAreas) {
        HiMineInfo hiMineInfo = new HiMineInfo();
        // 找到对应的工作面
        Optional<Area> stressArea = listAreas.stream().filter(area -> area.getName().equals(stress.getAreaname())).findFirst();
        if (stressArea.isPresent()) {
            hiMineInfo.setAreaId(stressArea.get().getId());
        } else {
            hiMineInfo.setAreaId(AreaUtil.getAllMineId(customDB));
        }
        hiMineInfo.setType((short) 1);
        hiMineInfo.setAcquisitionTime(stress.getCollectiontime());
        hiMineInfo.setStressValue(stress.getValue());
        hiMineInfo.setQuakeValue((double) 0);
        hiMineInfo.setMemo(stress.getMemo());
        // 找到对应的测点
        Optional<StressMeasurePoint> stressMeasurePoint = liStressMeasurePoints.stream()
                .filter(
                        stressMp -> Objects.equals(stressMp.getTunnelName(), stress.getTunnelname()) &&
                                Objects.equals(stressMp.getDepth(), stress.getDepth()) &&
                                Objects.equals(stressMp.getDistance(), stress.getDistance()))
                .findFirst();
        stressMeasurePoint.ifPresent(mp -> hiMineInfo.setMpId(mp.getId()));
        return hiMineInfo;
    }

    /**
     * 获取Stress预警状态
     */
    public Short getWarnStatus(Double PValue) {
        short warnStatus;
        if (PValue < 9) {
            warnStatus = 0;
        } else if (PValue > 9 && PValue < 11) {
            warnStatus = 1;
        } else {
            warnStatus = 2;
        }
        return warnStatus;
    }

    /**
     * 获取微震预警等级
     */
    public String getQuakeWarn(String customDB, Double energy, String quakeWarnConfig) {
        String warnStatus = "";
        DynamicDataSourceContextHolder.setDataSource(customDB);
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

    public ConnStatus getStressConStatus(String customDB, Mine mine, Date stressTopNewDate, int topNewDate) {
        ConnStatus connStatus = getConnStatus(customDB, mine, stressTopNewDate, topNewDate);
        if (connStatus == null) return null;
        connStatus.setType("7");
        return connStatus;
    }

    public ConnStatus getQuakeConStatus(String customDB, Mine mine, Date quakeTopNewDate, int quakeTopTimeOut) {
        ConnStatus connStatus = getConnStatus(customDB, mine, quakeTopNewDate, quakeTopTimeOut);
        if (connStatus == null) return null;
        connStatus.setType("6");
        return connStatus;
    }

    public ConnStatus getConnStatus(String customDB, Mine mine, Date stressTopNewDate, int topNewDate) {
        ConnStatus connStatus = new ConnStatus();
        DynamicDataSourceContextHolder.setDataSource(customDB);
        Short warnValue = areaTopDataInfoMapper.findAreaValue();
        DynamicDataSourceContextHolder.setDataSource("1000");
        String IDAndName = organMineMapper.findIDByName(mine.getName() + "%");
        if (IDAndName == null) {
            return null;
        } else {
            connStatus.setMineCode(IDAndName.substring(0, IDAndName.indexOf(",")));
        }
        connStatus.setMineName(IDAndName.substring(IDAndName.indexOf(",") + 1));
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

        if (String.valueOf(warnValue) == null) {
            connStatus.setWarningDetail("无数据");
        } else {
            connStatus.setWarningDetail(String.valueOf(warnValue));
        }
        connStatus.setAcquireTime(stressTopNewDate);
        connStatus.setMemo("连接状态");
        return connStatus;
    }
}
