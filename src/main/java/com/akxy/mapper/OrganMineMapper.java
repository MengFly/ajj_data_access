package com.akxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.OrganMine;

@Mapper
public interface OrganMineMapper {

	/**
	 * 根据矿名查看ID
	 * 
	 * @param name
	 * @return
	 */
	String findIDByName(@Param("name") String name);

	int deleteByPrimaryKey(Short id);

	int insert(OrganMine record);

	int insertSelective(OrganMine record);

	OrganMine selectByPrimaryKey(Short id);

	int updateByPrimaryKeySelective(OrganMine record);

	int updateByPrimaryKey(OrganMine record);
}