package com.akxy.mapper;

import com.akxy.entity.MineInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CurMineInfoMapper {
	
	/**
	 * 遍历CURMINE_INFO表
	 */
	List<MineInfo> getAllCurMine();
	
    
	/**
	 * 写入预警信息
	 */
	void insertAll(List<MineInfo> curMineInfo);
	
	/**
	 * 更新已存在的数据
	 */
	void updateCurMine(MineInfo curMineInfo);

	int countByAreaIdAndMpId(@Param("areaId") Long areaId,@Param("mpId") Long mpId);
}