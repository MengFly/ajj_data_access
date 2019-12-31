package com.akxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.Config;

@Mapper
public interface ConfigMapper {
    
	/**
	 * 查询微震预警配置
	 * @return
	 */
//	public String getQuakeWarnCon();
	
	/**
	 * 获取配置信息
	 * @param type
	 * @param name
	 * @return
	 */
	public Config getConfigInfo(@Param("type")String type,@Param("name")String name);
	
}