package com.akxy.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.Area;

@Mapper
public interface AreaMapper {
	
	/**
	 * 根据中间库的数据中areaNames字段获取areaId
	 * @param areaName 区域名称
	 * @return areaId  区域编码
	 */
	 Long getAreaIdByAreaName(@Param("name")String areaName);
	
	/**
	 * 向Area表中添加数据
	 * @param area 数据的entity
	 * @return 返回1：插入成功；返回0：插入失败
	 */
	 int insertData(Area area);
	
	/**
	 * 获取AreaId
	 * @return
	 */
     List<Long> getAllID();
    
    /**
     * 根据AreaName判断是否存在改区域配置
     * @param areaName
     * @return
     */
     Integer judgeContain(@Param("areaName")String areaName);
    
    /**
          * 获取Area列表
     * @return
     */
     List<Area> getArea();
    
    /**
     * 批量插入Area
     * @param listAreas
     */
     void insertGroupArea(List<Area> listAreas);
    
	
}