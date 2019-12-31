package com.akxy.common;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.Area;
import com.akxy.entity.AreaDataInfo;
import com.akxy.entity.AreaTopDataInfo;
import com.akxy.entity.ConnStatus;
import com.akxy.entity.CurMineInfo;
import com.akxy.entity.HiMineInfo;
import com.akxy.entity.Mine;
import com.akxy.entity.PointSign;
import com.akxy.entity.PosResult;
import com.akxy.entity.Quake;
import com.akxy.entity.Stress;
import com.akxy.entity.StressDataInfo;
import com.akxy.entity.StressMeasurePoint;
import com.akxy.entity.StressTopDataInfo;
import com.akxy.mapper.AreaMapper;
import com.akxy.mapper.AreaTopDataInfoMapper;
import com.akxy.mapper.ConfigMapper;
import com.akxy.mapper.CurMineInfoMapper;
import com.akxy.mapper.HiMineInfoMapper;
import com.akxy.mapper.MineMapper;
//import com.akxy.mapper.MineMapper;
import com.akxy.mapper.OrganMineMapper;
import com.akxy.mapper.PosResultMapper;
import com.akxy.mapper.QuakeMapper;
import com.akxy.mapper.StressDataInfoMapper;
import com.akxy.mapper.StressMapper;
import com.akxy.mapper.StressMeasurePointMapper;
import com.akxy.mapper.StressTopDataInfoMapper;
import com.akxy.service.impl.DataAccessServiceImpl;
import com.akxy.util.CopyUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据操作工具类
 * 
 * @author cqz 2019-3-11
 *
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DataUtil {

	private static DataUtil dataUtil = new DataUtil();
	@Autowired
	private AreaMapper areaMapper;
	@Autowired
	private StressMeasurePointMapper stressMeasurePointMapper;
	@Autowired
	private StressDataInfoMapper stressDataInfoMapper;
	@Autowired
	private StressMapper stressMapper;
	@Autowired
	private StressTopDataInfoMapper stressTopDataInfoMapper;
	@Autowired
	private QuakeMapper quakeMapper;
	@Autowired
	private PosResultMapper posResultMapper;
	@Autowired
	private CurMineInfoMapper curMineInfoMapper;
	@Autowired
	private HiMineInfoMapper hiMineInfoMapper;
	@Autowired
	private ConfigMapper configMapper;
	@Autowired
	private MineMapper mineMapper;
	@Autowired
	private OrganMineMapper organMineMapper;
	@Autowired
	private AreaTopDataInfoMapper areaTopDataInfoMapper;

	public static DataAccessServiceImpl dataAccessServiceImpl = new DataAccessServiceImpl();

	@PostConstruct
	public void init() {
		dataUtil = this;
		dataUtil.areaMapper = this.areaMapper;
		dataUtil.stressMeasurePointMapper = this.stressMeasurePointMapper;
		dataUtil.stressDataInfoMapper = this.stressDataInfoMapper;
		dataUtil.stressMapper = this.stressMapper;
		dataUtil.stressTopDataInfoMapper = this.stressTopDataInfoMapper;
		dataUtil.posResultMapper = this.posResultMapper;
		dataUtil.quakeMapper = this.quakeMapper;
		dataUtil.curMineInfoMapper = this.curMineInfoMapper;
		dataUtil.hiMineInfoMapper = this.hiMineInfoMapper;
		dataUtil.configMapper = this.configMapper;
		dataUtil.mineMapper = this.mineMapper;
		dataUtil.organMineMapper = this.organMineMapper;
	}

	/**
	 * 1、检测STRESS_MEASUREPOINT表中是否已经录入该测点； 2、若没有该测点则生成新测点MP_ID； 3、反之则return。
	 * 
	 * @param stress  需要写入到子数据库的应力数据
	 * @param maxMPID STRESS_MEASUREPOINT表中现有的最大MP_ID
	 * @return 新增的MP_ID
	 */
	public void getMeasurPointId(List<StressMeasurePoint> exitsStressMeasurePoints, List<Area> listArea,
			List<Stress> stresses, String primaryDB, String customDB, List<PointSign> pointSigns) {
		DynamicDataSourceContextHolder.setDataSource(customDB);
		List<Long> tempList = exitsStressMeasurePoints.stream().map(StressMeasurePoint::getId)
				.collect(Collectors.toList());
		Collections.sort(tempList);
		Collections.reverse(tempList);
		List<StressMeasurePoint> stressMeasurePoints = optionPoint(exitsStressMeasurePoints, listArea, stresses,
				tempList, primaryDB, customDB);
		List<String> signList = new ArrayList<>();
		stressMeasurePoints.forEach(e -> {
			if (tempList.size() == 0) {
				e.setId(Long.valueOf(customDB.substring(customDB.lastIndexOf("0") + 1, customDB.length()) + "0001"));
			} else {
				e.setId(tempList.get(0) + 1);
			}
			tempList.add(0, e.getId());
			if (signList.contains(e.getTunnelName() + e.getDepth() + e.getDistance())) {

			} else {
				DynamicDataSourceContextHolder.setDataSource(customDB);
				dataUtil.stressMeasurePointMapper.writeMeasurePoint(e);
				signList.add(e.getTunnelName() + e.getDepth() + e.getDistance());
			}
		});
	}
//		}

	/**
	 * 组装测点信息
	 * 
	 * @param listPointSign
	 */
	public List<StressMeasurePoint> optionPoint(List<StressMeasurePoint> stressMeasurePoints, List<Area> listArea,
			List<Stress> stresses, List<Long> tempList, String primaryDB, String customDB) {
		List<StressMeasurePoint> returnPoints = new ArrayList<>();
		try {
			DynamicDataSourceContextHolder.setDataSource(primaryDB);
			List<StressMeasurePoint> temp = new ArrayList<>();
			List<Stress> stressesList = stresses;
			List<String> signList = new ArrayList<String>();
			nextData: for (Stress stress : stressesList) {
				StressMeasurePoint stressMeasurePoint = new StressMeasurePoint();
				String sign = stress.getTunnelname() + stress.getDepth() + stress.getDistance();
				if (signList.contains(sign) == true) {
					continue nextData;
				}
				boolean flag = stressMeasurePoints.stream().anyMatch(
						point -> (point.getTunnelName() + point.getDepth() + point.getDistance()).equals(sign));
//				Integer num = stressMeasurePoints.stream()
//						.filter(e -> (e.getTunnelName() + e.getDepth() + e.getDistance()).equals(sign))
//						.collect(Collectors.toList()).size();
				if (flag) {
					continue nextData;
				}
				signList.add(sign);
				if (stressMeasurePoints != null) {
					temp = stressMeasurePoints.stream()
							.filter(e -> e.getTunnelName().equals(stress.getTunnelname())
									&& e.getDepth() == stress.getDepth() && e.getDistance() == stress.getDistance())
							.collect(Collectors.toList());
				}
				if (temp.size() == 0) {
					List<Area> listAreaByName = listArea.stream().filter(e -> e.getName().equals(stress.getAreaname()))
							.collect(Collectors.toList());
					if (listAreaByName.size() == 0) {
						stressMeasurePoint.setAreaId(
								Long.valueOf(customDB.substring(customDB.lastIndexOf("0") + 1, customDB.length())
										+ "000".toString()));
					} else {
						stressMeasurePoint.setAreaId(listAreaByName.get(0).getId());
					}
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
					if (("").equals(stress.getMemo()) || stress.getMemo() == null) {
						stress.setMemo("0");
					}
					stressMeasurePoint.setMemo(stress.getMemo());
					stressMeasurePoint.setFromTime(new Date());
					stressMeasurePoint.setToTime(new Date());
					returnPoints.add(stressMeasurePoint);
				}
			}
			return returnPoints;
		} catch (Exception e) {
			log.error("STRESS=>{}", e);
		}

		return returnPoints;
	}

	/**
	 * 组装StressDataInfo信息
	 * 
	 * @param stress 经过处理的中间库Stress信息
	 * @return List<StressDataInfo> 组装完成的StressDataInfo信息
	 */
	public static StressDataInfo getStressDataInfo(String customDB, Stress stress,
			List<StressMeasurePoint> stressMeasurePoints) {
		StressDataInfo stressDataInfo = new StressDataInfo();
		try {
			stressDataInfo.setAcquisitionTime(stress.getCollectiontime());
			List<Area> listArea = (List<Area>) dataAccessServiceImpl.mapCache.get("AREA" + customDB);
			if (listArea == null) {
				listArea = dataUtil.areaMapper.getArea();
			}
			// 结果集
			List<Area> listAreaByName = listArea.stream().filter(e -> e.getName().equals(stress.getAreaname()))
					.collect(Collectors.toList());
			if (listAreaByName.size() == 0) {
//				Area area = listArea.get(0);
				stressDataInfo.setAreaId(listArea.get(0).getId());
			} else {
				stressDataInfo.setAreaId(listAreaByName.get(0).getId());
			}
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
			stressDataInfo.setWarnStatus(dataUtil.getWarnStatus(stress.getValue()));
//			stressDataInfo.setZfIndex((stress.getValue() - stress.getInitialvalue()) / stress.getInitialvalue());
		} catch (Exception e) {
			log.info("{},{} EXCEPTION=>{}", customDB, stress, e);
			return null;
		}
		return stressDataInfo;
	}

	/**
	 * 组装StressTopDataInfo信息
	 * 
	 * @param MPID
	 * @param stress
	 * @return
	 */
	public StressTopDataInfo assembleStressTop(String customDB, Long MPID, Stress stress) {
		DynamicDataSourceContextHolder.setDataSource(stress.getMinecode());
		StressTopDataInfo stressTopDataInfo = new StressTopDataInfo();
		stressTopDataInfo.setAcquisitionTime(stress.getCollectiontime());
		stressTopDataInfo.setMpName(stress.getName());
		List<Area> listArea = (List<Area>) dataAccessServiceImpl.mapCache.get("AREA" + customDB);
		if (listArea == null) {
			DynamicDataSourceContextHolder.setDataSource(stress.getMinecode());
			listArea = dataUtil.areaMapper.getArea();
		}
		// 使用lambda表达式， 可以从listArea中过滤出符合条件的结果。
		// 需要筛选的条件：从listArea中筛选出对应AreaName的结果
		List<String> listName = new ArrayList<>();
		listName.add(stress.getAreaname());
		// 结果集
		List<Area> listAreaByName = new ArrayList<>();
		listAreaByName = listArea.stream().filter((Area area) -> listName.contains(area.getName()))
				.collect(Collectors.toList());
		stressTopDataInfo.setAreaId(listAreaByName.get(0).getId());
		stressTopDataInfo.setpValue(stress.getValue());
		stressTopDataInfo.setX(stress.getX());
		stressTopDataInfo.setY(stress.getY());
		stressTopDataInfo.setZ(stress.getZ());
		stressTopDataInfo.setWarnStatus(getWarnStatus(stress.getValue()));
		stressTopDataInfo.setYellowValue(BigDecimal.valueOf(stress.getYellowwarn()));
		stressTopDataInfo.setRedValue(BigDecimal.valueOf(stress.getRedwarn()));
		stressTopDataInfo.setDepth(BigDecimal.valueOf(stress.getDepth()));
		stressTopDataInfo.setDistance(BigDecimal.valueOf(stress.getDistance()));
		stressTopDataInfo.setMemo(stress.getMemo());
		stressTopDataInfo.setMpId(MPID);
		return stressTopDataInfo;
	}

	/**
	 * 组装POSResult数据
	 * 
	 * @param quake
	 * @return
	 */
	public PosResult assemblePosResult(List<Area> areas, String customDB, Quake quake) {
		PosResult posResult = new PosResult();
		try {
			posResult.setCollectTime(quake.getCollectiontime());
			posResult.setX(quake.getX());
			posResult.setY(quake.getY());
			posResult.setZ(quake.getZ());
			posResult.setEnergy(quake.getEnergy());
//			List<Area> listArea = (List<Area>) dataAccessServiceImpl.mapCache.get("AREA" + customDB);
			boolean flag = areas.stream().anyMatch(area -> area.getName().equals(quake.getAreaname()));
			if (flag == true) {
				posResult.setAreaId(areas.stream().filter(a -> a.getName().equals(quake.getAreaname()))
						.collect(Collectors.toList()).get(0).getId());
			} else {
				posResult.setAreaId(
						Long.valueOf(customDB.substring(customDB.lastIndexOf("0"), customDB.length()) + "000"));
			}
			List<String> listName = new ArrayList<>();
			listName.add(quake.getAreaname());
			posResult.setSource(getQuakeWarn(quake.getMinecode(), quake.getEnergy()));
			posResult.setMemo("0");
			// 临矿上传所有字段后再打开
//			posResult.setAvgswing(Long.valueOf(quake.getAvgswing().toString()));
//			posResult.setMaxswing(Long.valueOf(quake.getMaxswing().toString()));
//			posResult.setBasicfreq(Long.valueOf(quake.getBasicfreq().toString()));
//			posResult.setChcount(Long.valueOf(quake.getChcount()));
//			posResult.setExcstatus(quake.getExcstatus());
//			posResult.setInstallway(quake.getInstallway());
//			posResult.setPosdesc(quake.getPosdesc());
//			posResult.setSampfreq(Long.valueOf(quake.getSampfreq()));
//			posResult.setSamplength(Long.valueOf(quake.getSamplength()));
//			posResult.setSensitivity(Long.valueOf(quake.getSensitivity().toString()));
//			posResult.setSensordir(quake.getSensordir().toLowerCase());
//			posResult.setSensortype(quake.getSensortype());
//			posResult.setTrigch(quake.getTrigch());
//			LOGGER.info("微震："+posResult);
		} catch (Exception e) {
			log.error("QUAKE ERROR=>{},{}", customDB, e);
		}
		return posResult;
	}

	/**
	 * 遍历Stress集合 将数据写入STRESS_DATAINFO表
	 * 
	 * @param listStress
	 */
	public void optionStress(String customDB, List<Stress> listStress, List<String> isExistConditions) {
		List<StressDataInfo> listStressDataInfos = new ArrayList<>();
//		try {
		StringBuffer temp = new StringBuffer();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<StressMeasurePoint> stressMeasurePoints = dataUtil.stressMeasurePointMapper.getAllPoint();
		nextStress: for (Stress stress : listStress) {
			temp.delete(0, temp.length());
			if (stress != null) {
				// 筛选掉不合规数据
				if (stress.getValue() > 30) {
					continue nextStress;
				}
				DynamicDataSourceContextHolder.setDataSource(customDB);
				StressDataInfo stressDataInfo = DataUtil.getStressDataInfo(customDB, stress, stressMeasurePoints);
				if (stressDataInfo == null) {
					continue nextStress;
				}
				stressDataInfo.setMpId(dataUtil.stressMeasurePointMapper.getMPID(stress.getTunnelname(),
						stress.getDepth(), stress.getDistance()));
				temp.append((simpleDateFormat.format(stressDataInfo.getAcquisitionTime()) + "/"
						+ stressDataInfo.getMpId().toString() + "/" + stressDataInfo.getAreaId()));
				if (isExistConditions.contains(temp.toString()) == false) {
					isExistConditions.add(0, temp.toString());
					listStressDataInfos.add(stressDataInfo);
				}
			} else {
				log.info("STRESS为NULL");
			}
		}
		if (listStressDataInfos.size() != 0) {
			DynamicDataSourceContextHolder.setDataSource(customDB);
			try {
				dataAccessServiceImpl.mapCache.put("DATAINFO" + customDB, listStressDataInfos);
				dataUtil.stressDataInfoMapper.deleteDataInfos(listStressDataInfos);
				dataUtil.stressDataInfoMapper.insertGroupDataInfo(listStressDataInfos);
			} catch (Exception e) {
				log.error("INSERT 主键重复=>{},{}", customDB, e);
			}
		}
//		} catch (Exception e) {
//			LOGGER.info("插入历史数据异常：{}" + e);
//			e.printStackTrace();
//		}
		DynamicDataSourceContextHolder.restoreDataSource();
	}

	/**
	 * 遍历所有测点的最新数据，并写入STRESS_TOP_DATAINFO表中
	 * 
	 * @param listPointSign
	 */
	public void writeToTopByPoints(List<StressDataInfo> stressDataInfos, List<StressMeasurePoint> stressMeasurePoints,
			String customDB) throws Exception {
		DynamicDataSourceContextHolder.setDataSource(customDB);
		List<StressTopDataInfo> insertTopStress = new ArrayList<>();
		StringBuffer string = new StringBuffer();
		List<StressTopDataInfo> stressTopDataInfos = dataUtil.stressTopDataInfoMapper.stressTopDataInfos();
		for (StressMeasurePoint stressMeasurePoint : stressMeasurePoints) {
			string.delete(0, string.length());
			List<StressDataInfo> stressDataInfoList = stressDataInfos.stream()
					.filter(e -> e.getAreaId().equals(stressMeasurePoint.getAreaId())
							&& e.getMpId().equals(stressMeasurePoint.getId()))
					.collect(Collectors.toList());
			if (stressDataInfoList.size() != 0) {
				StressTopDataInfo stressTopDataInfo = new StressTopDataInfo();
				StressDataInfo stressDataInfo = stressDataInfoList.get(0);
				Integer temp = stressTopDataInfos.stream().filter(e -> e.getAreaId().equals(stressDataInfo.getAreaId())
						&& e.getMpId().equals(stressDataInfo.getMpId())).collect(Collectors.toList()).size();
				if (temp == 1) {
					CopyUtils.Copy(stressDataInfo, stressTopDataInfo);
					dataUtil.stressTopDataInfoMapper.updateTopData(stressTopDataInfo);
				} else {
					CopyUtils.Copy(stressDataInfo, stressTopDataInfo);
					insertTopStress.add(stressTopDataInfo);
				}
			}
		}
		try {
			if (insertTopStress.size() != 0) {
				DynamicDataSourceContextHolder.setDataSource(customDB);
				dataUtil.stressTopDataInfoMapper.insertGroupData(insertTopStress);
//				DynamicDataSourceContextHolder.restoreDataSource();
			}
		} catch (Exception e) {
			log.error("TOP INSERT ERROR=>{},{}", customDB, e);
		}
	}

	/**
	 * 写入或更新微震数据
	 * 
	 * @param listQuake
	 */
	public void optionQuake(List<Area> areas, String primaryDB, String customDB, List<Quake> listQuake) {
		List<PosResult> listINPosResult = new ArrayList<>();
		DynamicDataSourceContextHolder.setDataSource(customDB);
//		List<Date> times = new ArrayList<>();
		DynamicDataSourceContextHolder.setDataSource(customDB);
		hasNext: for (Quake quake : listQuake) {
			PosResult posResult = dataUtil.assemblePosResult(areas, customDB, quake);
			if (posResult == null) {
				continue hasNext;
			}
//			List<Date> conditions = new ArrayList<>();
//			conditions.add(posResult.getCollectTime());
//			if (times.contains(posResult.getCollectTime())) {
//				continue hasNext;
//			}
			listINPosResult.add(posResult);
		}
		DynamicDataSourceContextHolder.restoreDataSource();
		DynamicDataSourceContextHolder.setDataSource(customDB);
		if (listINPosResult.size() != 0) {
			try {
				dataUtil.posResultMapper.insertGroupData(listINPosResult);
			} catch (Exception e) {
				log.error("dataUtil.posResultMapper INSERT =>{}", e);
			}
		}
		DynamicDataSourceContextHolder.restoreDataSource();
	}

	/**
	 * 得到微震数据
	 * 
	 * @return
	 */
	public List<Quake> getQuake() {
//		List<Quake> listQuake = dataUtil.quakeMapper.readQuakeData();
		return null;
	}

	/**
	 * 写入实时预警数据
	 * 
	 * @param listPointSign
	 */
	public void optionCurMine(String customDB, List<Stress> listStress,
			List<StressMeasurePoint> liStressMeasurePoints) {
		DynamicDataSourceContextHolder.setDataSource(customDB);
		List<CurMineInfo> curMineInfos = dataUtil.curMineInfoMapper.getAllCurMine();
		DynamicDataSourceContextHolder.setDataSource("ds0");
		for (Stress stress : listStress) {
			if (stress.getValue() >= 9) {
				CurMineInfo curMineInfo = getCurMineInfo(customDB, stress);
				DynamicDataSourceContextHolder.setDataSource(customDB);
//				Integer temp = dataUtil.curMineInfoMapper.judgeContain(curMineInfo.getAreaId(), curMineInfo.getMpId());
//				Integer temp = curMineInfos.stream()
//						.filter(e -> e.getAreaId() == curMineInfo.getAreaId() && e.getMpId() == curMineInfo.getMpId())
//						.collect(Collectors.toList()).size();
				boolean flag = curMineInfos.stream().anyMatch(
						e -> e.getAreaId() == curMineInfo.getAreaId() && e.getMpId() == curMineInfo.getMpId());
				if (flag == true) {
					dataUtil.curMineInfoMapper.updateCurMine(curMineInfo);
				} else {
					dataUtil.curMineInfoMapper.writeToCurMine(curMineInfo);
				}
				DynamicDataSourceContextHolder.restoreDataSource();
			}
		}
	}

	/**
	 * 组装实时预警信息
	 * 
	 * @param stress
	 * @return
	 */
	public CurMineInfo getCurMineInfo(String customDB, Stress stress) {
		if (stress == null) {
			return null;
		}
		DynamicDataSourceContextHolder.setDataSource(customDB);
		List<Area> listArea = dataUtil.areaMapper.getArea();
//		if (listArea == null) {
//			DynamicDataSourceContextHolder.setDataSource(stress.getMinecode());
//			listArea = dataUtil.areaMapper.getArea();
//		}
		List<String> listName = new ArrayList<>();
		listName.add(stress.getAreaname());
		List<Area> listAreaByName = new ArrayList<>();
		listAreaByName = listArea.stream().filter((Area area) -> listName.contains(area.getName()))
				.collect(Collectors.toList());
		DynamicDataSourceContextHolder.setDataSource(customDB);
		CurMineInfo curMineInfo = new CurMineInfo();
		if (listAreaByName.size() == 0) {
			curMineInfo.setAreaId(
					Long.valueOf(customDB.substring(customDB.lastIndexOf("0") + 1, customDB.length()) + "000"));
		} else {
			curMineInfo.setAreaId(listAreaByName.get(0).getId());
		}
		curMineInfo.setType((short) 1); // 0 微震 1应力
		curMineInfo.setAcquisitionTime(stress.getCollectiontime());
		curMineInfo.setStressValue(stress.getValue());
		curMineInfo.setQuakeValue((double) 0);
		curMineInfo.setMemo(stress.getMemo());
		List<StressMeasurePoint> liStressMeasurePoints = new ArrayList<>();
		liStressMeasurePoints = (List<StressMeasurePoint>) dataAccessServiceImpl.mapCache.get("POINT" + customDB);
		if (liStressMeasurePoints == null) {
			liStressMeasurePoints = dataUtil.stressMeasurePointMapper.getAllPoint();
		}
		String tunName = "";
		Double depth = 0.0;
		Double distance = 0.0;
		for (StressMeasurePoint stressMeasurePoint : liStressMeasurePoints) {
			tunName = stressMeasurePoint.getTunnelName();
			depth = stressMeasurePoint.getDepth();
			distance = stressMeasurePoint.getDistance();
			if (tunName.equals(stress.getTunnelname()) && depth.equals(stress.getDepth())
					&& distance.equals(stress.getDistance())) {
				curMineInfo.setMpId(stressMeasurePoint.getId());
			}
		}
		if (curMineInfo.getMpId() == null) {
			curMineInfo.setMpId(0L);
		}
//		curMineInfo.setMpId(dataUtil.stressMeasurePointMapper.getMPID(stress.getTunnelname(), stress.getDepth(),
//				stress.getDistance()));
		DynamicDataSourceContextHolder.restoreDataSource();
		return curMineInfo;
	}

	/**
	 * 写入历史预警表
	 * 
	 * @param stresses
	 */
	public void optionHiMine(String customDB, List<Stress> stresses, List<StressMeasurePoint> liStressMeasurePoints,
			List<Area> listAreas) {
		List<HiMineInfo> listHimineInfos = new ArrayList<>();
		List<Area> areas = null;
		for (Stress stress : stresses) {
			if (stress != null) {
				HiMineInfo hiMineInfo = getHiMineInfo(customDB, stress, liStressMeasurePoints, listAreas);
//				hiMineInfo.setAreaId(1000L);
				listHimineInfos.add(hiMineInfo);
			}
		}
		try {
			if (listHimineInfos.size() > 0) {
				DynamicDataSourceContextHolder.setDataSource(customDB);
				dataUtil.hiMineInfoMapper.insertGroupHi(listHimineInfos);
			}
		} catch (Exception e) {
			log.info("插入历史表数据失败：{}" + e);
		}
	}

	/**
	 * 组装历史预警数据
	 * 
	 * @param stress
	 * @return
	 */
	public HiMineInfo getHiMineInfo(String customDB, Stress stress, List<StressMeasurePoint> liStressMeasurePoints,
			List<Area> listAreas) {
		DynamicDataSourceContextHolder.setDataSource(stress.getMinecode());
		HiMineInfo hiMineInfo = new HiMineInfo();
		List<Area> listArea = (List<Area>) dataAccessServiceImpl.mapCache.get("AREA" + customDB);
		if (listArea == null) {
			DynamicDataSourceContextHolder.setDataSource(stress.getMinecode());
			listArea = dataUtil.areaMapper.getArea();
		}
		// 使用lambda表达式， 可以从listArea中过滤出符合条件的结果。
		// 需要筛选的条件：从listArea中筛选出对应AreaName的结果
		List<String> listName = new ArrayList<>();
		listName.add(stress.getAreaname());
		// 结果集
		List<Area> areas = new ArrayList<>();
		boolean flag = listAreas.stream().anyMatch(e -> e.getName().equals(stress.getAreaname()));
		if (flag == false) {
			hiMineInfo.setAreaId(
					Long.valueOf(customDB.substring(customDB.lastIndexOf("0") + 1, customDB.length()) + "000"));
		} else {
			hiMineInfo.setAreaId(areas.get(0).getId());
		}
		hiMineInfo.setType((short) 1);
		hiMineInfo.setAcquisitionTime(stress.getCollectiontime());
		hiMineInfo.setStressValue(stress.getValue());
		hiMineInfo.setQuakeValue((double) 0);
		hiMineInfo.setMemo(stress.getMemo());
		String tunName = "";
		Double depth = 0.0;
		Double distance = 0.0;
		for (StressMeasurePoint stressMeasurePoint : liStressMeasurePoints) {
			tunName = stressMeasurePoint.getTunnelName();
			depth = stressMeasurePoint.getDepth();
			distance = stressMeasurePoint.getDistance();
			if (tunName.equals(stress.getTunnelname()) && depth.equals(stress.getDepth())
					&& distance.equals(stress.getDistance())) {
				hiMineInfo.setMpId(stressMeasurePoint.getId());
			}
		}
		DynamicDataSourceContextHolder.restoreDataSource();
		return hiMineInfo;
	}

	public Area getArea(Long areaId, String areaName) {
		Area area = new Area();
		area.setId(areaId + 1);
		area.setIsmonitor((short) 1);
		area.setName(areaName);
		area.setPosList(null);
		area.setMemo(null);
		area.setType("1");
		return area;
	}

	/**
	 * 获取Stress预警状态
	 * 
	 * @param PValue
	 * @return
	 */
	public Short getWarnStatus(Double PValue) {
		Short warnStatus = 0;
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
	 * 
	 * @param energy
	 * @return
	 */
	public String getQuakeWarn(String customDB, Double energy) {
		String warnStatus = "";
		DynamicDataSourceContextHolder.setDataSource(customDB);
		String[] temp = dataUtil.configMapper.getConfigInfo("WZ", "WARNING").getStrValue().split(",");
		if (energy < Integer.valueOf(temp[0])) {
			warnStatus = "0";
		} else if (energy > Integer.valueOf(temp[0]) && energy < Integer.valueOf(temp[1])) {
			warnStatus = "1";
		} else {
			warnStatus = "2";
		}
		return warnStatus;
	}

	public AreaDataInfo getAreaDataInfo(String customDB, Area area, Double maxValue, Double maxEnergy,
			String[] warnParams) {
		Short quakeValue = Short.valueOf(getQuakeWarn(customDB, maxEnergy));
		Short stressValue = getWarnStatus(maxValue);
		AreaDataInfo areaDataInfo = new AreaDataInfo();
		areaDataInfo.setAreaId(area.getId());
		areaDataInfo.setAcquisitionTime(new Date());
		areaDataInfo.setQuakeValue(quakeValue);
		areaDataInfo.setStressValue(stressValue);
		areaDataInfo.setAreaValue(Short.valueOf(getAREAVALUE(stressValue, quakeValue)));
		areaDataInfo.setAreaLevel(getAREALevel(stressValue, quakeValue, warnParams));
		areaDataInfo.setMemo(null);
		return areaDataInfo;
	}

	public Short getAREAVALUE(Short maxValue, Short maxEnergy) {
		Short areaValue = (short) (maxEnergy / 2 + maxValue / 2);
		return areaValue;
	}

	public String getAREALevel(Short maxValue, Short maxEnergy, String[] warnParams) {
		String areaLevel = "";
		Short areaValue = (short) (maxEnergy / 2 + maxValue / 2);
		if (areaValue == 0) {
			areaLevel = "无数据";
		} else if (areaValue < Double.parseDouble(warnParams[0])) {
			areaLevel = "危险等级：D";
		} else if (areaValue > Double.parseDouble(warnParams[0]) && areaValue <= Double.parseDouble(warnParams[1])) {
			areaLevel = "危险等级：C";
		} else if (areaValue > Double.parseDouble(warnParams[1]) && areaValue <= Double.parseDouble(warnParams[2])) {
			areaLevel = "危险等级：B";
		} else if (areaValue > Double.parseDouble(warnParams[2])) {
			areaLevel = "危险等级：A";
		}
		return areaLevel;
	}

	public AreaTopDataInfo getTopArea(AreaDataInfo areaDataInfo) {
		AreaTopDataInfo areaTopDataInfo = new AreaTopDataInfo();
		areaTopDataInfo.setAreaId(areaDataInfo.getAreaId());
		areaTopDataInfo.setAcquisitionTime(areaDataInfo.getAcquisitionTime());
		areaTopDataInfo.setAreaLevel(areaDataInfo.getAreaLevel());
		areaTopDataInfo.setAreaValue(areaDataInfo.getAreaValue());
		areaTopDataInfo.setQuakeValue(areaDataInfo.getQuakeValue());
		areaTopDataInfo.setStressValue(areaDataInfo.getStressValue());
		areaTopDataInfo.setMemo(areaDataInfo.getMemo());
		return areaTopDataInfo;
	}

	public Map<String, Double> getMaxEnergyAndValue(List<Quake> quakes, List<Stress> stress, Area area) {
		Map<String, Double> resultMap = new HashMap<>();
		List<String> listCondition = new ArrayList<>();
		String condition = area.getName();
		if (condition.equals("全矿")) {
			condition = "-1";
		}
		listCondition.add(condition);
		List<Quake> energys = quakes.stream().filter((Quake quake) -> listCondition.contains(quake.getAreaname()))
				.collect(Collectors.toList());
		Double maxEnergy = 0.0;
		for (int i = 0; i < energys.size() - 1; i++) {
			if (energys.get(i).getEnergy() < energys.get(i + 1).getEnergy()) {
				maxEnergy = energys.get(i + 1).getEnergy();
			}
		}
		resultMap.put("MAXENERGY", maxEnergy);
		List<Stress> values = stress.stream().filter((Stress s) -> listCondition.contains(s.getAreaname()))
				.collect(Collectors.toList());
		Double maxValue = 0.0;
		for (int i = 0; i < values.size() - 1; i++) {
			if (values.get(i).getValue() < values.get(i + 1).getValue()) {
				maxValue = values.get(i + 1).getValue();
			}
		}
		resultMap.put("MAXVALUE", maxValue);
//		LOGGER.info(condition+"  "+resultMap);
		return resultMap;
	}

	public ConnStatus getStressConStatus(String customDB, List<CurMineInfo> listCurMine, Mine mine) {
		ConnStatus connStatus = new ConnStatus();
		Short warnValue = dataUtil.areaTopDataInfoMapper.findAreaValue();
		// 获取带有应力预警信息的CurMine信息
//		List<CurMineInfo> stressWarn = listCurMine.stream().filter(curMineInfo -> curMineInfo.getStressValue() > 0)
//				.collect(Collectors.toList());
		DynamicDataSourceContextHolder.setDataSource("1000");
		String IDAndName = dataUtil.organMineMapper.findIDByName(mine.getName() + "%");
		if (IDAndName == null) {
			return null;
		} else {
			connStatus.setMineCode(IDAndName.substring(0, IDAndName.indexOf(",")));
		}
		List<String> listMineCode = new ArrayList<>();
		listMineCode.add(customDB);
		connStatus.setMineName(IDAndName.substring(IDAndName.indexOf(",") + 1, IDAndName.length()));
		DynamicDataSourceContextHolder.setDataSource(customDB);
		Date newTime = dataUtil.stressTopDataInfoMapper.findNewDate();
		int timeout = Integer.valueOf(dataUtil.configMapper.getConfigInfo("TIME", "STRESSTIMEOUT").getStrValue());
		Date curDate = new Date();
		int deviationStress = 1000;
		if (newTime != null) {
			deviationStress = (int) ((curDate.getTime() - newTime.getTime()) / (1000 * 60));
			if (deviationStress > timeout) {
				connStatus.setConnStatus("0");
			} else {
				connStatus.setConnStatus("1");
			}
		} else {
			connStatus.setConnStatus("0");
		}
		connStatus.setType("7");
		if (String.valueOf(warnValue) == null) {
			connStatus.setWarningDetail("无数据");
		} else {
			connStatus.setWarningDetail(String.valueOf(warnValue));
		}
		connStatus.setAcquireTime(newTime);
		connStatus.setMemo("连接状态");
		return connStatus;
	}

	public ConnStatus getQuakeConStatus(String customDB, List<CurMineInfo> listCurMine, Mine mine) {
		ConnStatus connStatus = new ConnStatus();
		Short warnValue = dataUtil.areaTopDataInfoMapper.findAreaValue();
		// 获取带有应力预警信息的CurMine信息
//		List<CurMineInfo> stressWarn = listCurMine.stream().filter(curMineInfo -> curMineInfo.getStressValue() > 0)
//				.collect(Collectors.toList());
		DynamicDataSourceContextHolder.setDataSource("1000");
		String IDAndName = dataUtil.organMineMapper.findIDByName(mine.getName() + "%");
		if (IDAndName == null) {
			return null;
		} else {
			connStatus.setMineCode(IDAndName.substring(0, IDAndName.indexOf(",")));
		}
		List<String> listMineCode = new ArrayList<>();
		listMineCode.add(customDB);
		connStatus.setMineName(IDAndName.substring(IDAndName.indexOf(",") + 1, IDAndName.length()));
		DynamicDataSourceContextHolder.setDataSource(customDB);
		Date newTimeDate = dataUtil.posResultMapper.findNewDate();
		int timeout = Integer.valueOf(dataUtil.configMapper.getConfigInfo("TIME", "QUAKETIMEOUT").getStrValue());
		Date curDate = new Date();
		int deviationQuake = 0;
		if (newTimeDate != null) {
			deviationQuake = (int) ((curDate.getTime() - newTimeDate.getTime()) / (1000 * 60));
			if (deviationQuake > timeout) {
				connStatus.setConnStatus("0");
			} else {
				connStatus.setConnStatus("1");
			}
		} else {
			connStatus.setConnStatus("0");
		}
		connStatus.setType("6");
		if (String.valueOf(warnValue) == null) {
			connStatus.setWarningDetail("无数据");
		} else {
			connStatus.setWarningDetail(String.valueOf(warnValue));
		}
		connStatus.setAcquireTime(newTimeDate);
		connStatus.setMemo("连接状态");
		DynamicDataSourceContextHolder.setDataSource("ds0");
		return connStatus;
	}

	/**
	 * 去除重复的Stress数据
	 * 
	 * @param list
	 * @return
	 */
	public List<Stress> removeDuplicate(List<Stress> list) {
		List<Stress> newList = new ArrayList<>();
		newList.add(list.get(0));
		for (int i = 0; i < list.size() - 1; i++) {
			boolean dTime = sameDate(list.get(i + 1).getCollectiontime(), list.get(i).getCollectiontime());
//			boolean dAreaName=list.get(i+1).getAreaname().equals(list.get(i).getAreaname());
			boolean dTunlName = list.get(i + 1).getTunnelname().equals(list.get(i).getTunnelname());
			Double dDepth = list.get(i + 1).getDepth() - list.get(i).getDepth();
			Double dDistance = list.get(i + 1).getDistance() - list.get(i).getDistance();
//			LOGGER.info(dDepth+"  "+dDistance);
//			LOGGER.info("是否去重:"+dTime+"  "+dTunlName+"  "+(dDepth==0.0)+"  "+(dDistance==0.0));
			if (!(dTime && dTunlName && dDepth == 0.0 && dDistance == 0.0)) {
//				list.remove(i+1);
				newList.add(list.get(i + 1));
			}
		}
		return newList;
	}

	/**
	 * 比较时间是否相等
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	private static boolean sameDate(Date d1, Date d2) {
		LocalDate localDate1 = ZonedDateTime.ofInstant(d1.toInstant(), ZoneId.systemDefault()).toLocalDate();
		LocalDate localDate2 = ZonedDateTime.ofInstant(d2.toInstant(), ZoneId.systemDefault()).toLocalDate();
		return localDate1.isEqual(localDate2);
	}

	/**
	 * 判断微震测点所属工作面
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x
	 * @param y
	 * @return
	 */
	public double isInLine(double x0, double y0, double x1, double y1, double x, double y) {
		return (x0 - x) * (y1 - y) - (y0 - y) * (x1 - x);
	};

	public boolean isInSquare(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
			double x, double y) {
		boolean b1 = isInLine(x0, y0, x1, y1, x, y) < 0.0;
		boolean b2 = isInLine(x1, y1, x2, y2, x, y) < 0.0;
		boolean b3 = isInLine(x2, y2, x3, y3, x, y) < 0.0;
		boolean b4 = isInLine(x3, y3, x0, y0, x, y) < 0.0;
		if (b1 && b2 && b3 && b4) {
			return true;
		}
		return false;
	}
//	for(Quake quake:quakes){
//	if (quake.getZ() <= searchEntity.getPositions()[9] && quake.getZ() >= searchEntity.getPositions()[8]) {
//		if (SpatialStatisticsUtil.isInSquare(searchEntity.getPositions()[0], searchEntity.getPositions()[1],
//				searchEntity.getPositions()[2], searchEntity.getPositions()[3], searchEntity.getPositions()[4],
//				searchEntity.getPositions()[5], searchEntity.getPositions()[6], searchEntity.getPositions()[7],
//				quake.getX(), quake.getY())) {
//			quakeTemps.add(quake);
//		}
//	}
//}

	public Double[] analysisPositions(String positions) {
		String[] single = positions.split(";");
		Double[] positionsDoubles = new Double[8];
		for (int a = 0; a < positionsDoubles.length; a++) {
			for (int i = 0; i < single.length; i++) {
				String[] XOrYString = single[i].split(",");
				for (int j = 0; j < XOrYString.length - 1; j++) {
					positionsDoubles[a] = Double.valueOf(XOrYString[j].toString());
				}
			}
		}
		return positionsDoubles;
	}

	// 判断该对象是否为null
//    public boolean isAllFieldNull(Object obj) throws Exception{
//        Class stuCla = (Class) obj.getClass();// 得到类对象
//        Field[] fs = stuCla.getDeclaredFields();//得到属性集合
//        boolean flag = true;
//        for (Field f : fs) {//遍历属性
//            f.setAccessible(true); // 设置属性是可以访问的(私有的也可以)
//            Object val = f.get(obj);// 得到此属性的值
//            if(val==null) {//只要有1个属性不为空,那么就不是所有的属性值都为空
//                flag = false;
//                break;
//            }
//        }
//        return flag;
//    }
//	
}
