package com.akxy.mapper;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.Quake;

@Mapper
public interface QuakeMapper {

	/**
	 * 批量删除已读微震
	 * 
	 * @param quakes
	 */
	void deleteGtoupData(List<Quake> quakes);

	/**
	 * 从中间库读取Quake数据
	 * 
	 * @return 返回Quake数据组成的List集合
	 */
	public List<Quake> readQuakeData(@Param("customDB") String customDB);

	/**
	 * 从中间库读取Quake数据
	 * 
	 * @return 返回Quake数据组成的List集合
	 */
	public List<Quake> findQuakesByMineCode(@Param("minecode") String customDB);

	/**
	 * 获取QUAKE表中所有AreaName
	 * 
	 * @return
	 */
	public List<String> getAllAreaName();

	/**
	 * 更新已读数据：添加已读标志
	 * 
	 * @param quake
	 */
	public void updateReadData(Quake quake);

	/**
	 * 通过areaName获取最大能量值
	 * 
	 * @param areaName
	 * @return
	 */
	public Double getEnergyByName(@Param("areaname") String areaname);

	/**
	 * 批量更新QUAKE表
	 * 
	 * @param quakes
	 */
	public void updateGroupData(List<Quake> quakes);

	/**
	 * 更新QUAKE表读取状态
	 */
	public void updateReadStatus(String customDB);

	Integer quakeCount();

    int deleteByTimeLessThan(@Param("time") Timestamp time);
}