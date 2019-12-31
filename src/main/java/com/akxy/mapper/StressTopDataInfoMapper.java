package com.akxy.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.StressTopDataInfo;

@Mapper
public interface StressTopDataInfoMapper {

	Date findNewDate();

	List<StressTopDataInfo> stressTopDataInfos();

	/**
	 * 获取MpNames
	 * 
	 * @return
	 */
	List<String> findAreaIdsAndMpIds();

	/**
	 * 批量更新
	 * 
	 * @param stressTopDataInfos
	 */
	public void updateGroupTopDataInfos(List<StressTopDataInfo> stressTopDataInfos);

	/**
	 * 写入Top表数据
	 * 
	 * @param stress 需要写入Top表的中间库数据
	 */
	public void writeTopDataInfo(StressTopDataInfo stressTopDataInfo);

	/**
	 * 依据MPID判断是否已经存在该测点数据
	 * 
	 * @param MPID
	 * @return
	 */
	public Integer judgeContainByMPID(@Param("areaId") Long areaId, @Param("MPID") Long MPID);

	/**
	 * 依据MPID删除已经存在的测点数据
	 * 
	 * @param MPID
	 */
	public void deleteTopDataByMPID(@Param("MPID") Long MPID);

	/**
	 * 批量插入Stress_Top_DataInfo
	 */
	public void insertGroupData(List<StressTopDataInfo> listTopStress);

	/**
	 * 更新Top表
	 * 
	 * @param stressTopDataInfo
	 */
	public void updateTopData(StressTopDataInfo stressTopDataInfo);

}