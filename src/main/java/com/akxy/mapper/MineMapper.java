package com.akxy.mapper;

import java.util.List;

import javax.validation.constraints.Min;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.Mine;
import com.akxy.entity.MineKey;

@Mapper
public interface MineMapper {
	
	/**
	 * 根据MineCode获取分数据库名称
	 * @param mineCode 分数据库编码
	 * @return 分数据库名字
	 */
	public String getMineNameByCode(@Param("MINECODE")String mineCode);
	
	/**
	 * 遍历数据库配置表
	 */
	List<Mine> listMines(String customDB);
	
	/**
	 * 获取NAME属性
	 * @param mineCode
	 * @return
	 */
	public String getName(@Param("mineCoude")String mineCode);
	
    
}