package com.akxy.mapper;

import com.akxy.entity.CurMineInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CurMineInfoMapper {
	
	/**
	 * 遍历CURMINE_INFO表
	 */
	List<CurMineInfo> getAllCurMine();
	
    
	/**
	 * 写入预警信息
	 */
	void writeToCurMine(CurMineInfo curMineInfo);
	
	/**
	 * 更新已存在的数据
	 */
	void updateCurMine(CurMineInfo curMineInfo);
	
}