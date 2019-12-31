package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.Area;
import com.akxy.entity.AreaDataInfo;

@Mapper
public interface AreaDataInfoMapper {
	
	/**
	 * 批量插入AREA_DATA_INFO
	 * @param listAreaDataInfo
	 */
    public void insertGroupData(List<AreaDataInfo> listAreaDataInfo);
    
    /**
     * 依据AreaId查询Top数据
     * @param areaId
     * @return
     */
    public AreaDataInfo getDataById(@Param("areaId")Long areaId);
    
}