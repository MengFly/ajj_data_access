package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.CurMineInfo;

@Mapper
public interface CurMineInfoMapper {
	
	/**
	 * 遍历CURMINE_INFO表
	 * @return
	 */
	public List<CurMineInfo> getAllCurMine();
	
    
	/**
	 * 写入预警信息
	 * @param curMineInfo
	 */
	public void writeToCurMine(CurMineInfo curMineInfo);
	
	/**
	 * 依照areaId判断该数据是否已经存在
	 * @param areaId
	 * @return
	 */
	public Integer judgeContain(@Param("areaId")Long areaId,@Param("mpId")Long mpId);
	
	/**
	 * 更新已存在的数据
	 * @param curMineInfo
	 */
	public void updateCurMine(CurMineInfo curMineInfo);
	
}