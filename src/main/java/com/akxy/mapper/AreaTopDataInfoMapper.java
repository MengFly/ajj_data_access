package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.AreaTopDataInfo;

@Mapper
public interface AreaTopDataInfoMapper {
	
	/**
	 * 获取全矿预警
	 * @return
	 */
	Short findAreaValue();
   
	/**
	 * 批量插入AREA_TOP_DATA_INFO
	 * @param listAreaTopData
	 */
	void insertGroupData(List<AreaTopDataInfo> listAreaTopData);
	
	/**
	 * 根据AreaId判断是否存在该区域数据
	 * @param areaId
	 * @return
	 */
	Integer judegContain(@Param("areaId") Long areaId);
	
	/**
	 * 更新已存在的数据
	 * @param areaTopDataInfo
	 */
	void updateTopData(AreaTopDataInfo areaTopDataInfo);
	
}