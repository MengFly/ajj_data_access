package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.HiMineInfo;

@Mapper
public interface HiMineInfoMapper {
	
	/**
	 * 写入预警信息
	 * @param hiMineInfo
	 */
	public void writeToHiMine(HiMineInfo hiMineInfo);
	
	/**
	 * 依照areaId判断该数据是否已经存在
	 * @param areaId
	 * @return
	 */
	public Integer judgeContain(@Param("areaId")Long areaId,@Param("mpId")Long mpId,@Param("type")Short type);
	
	/**
	 * 更新已存在的数据
	 * @param hiMineInfo
	 */
	public void updateHiMine(HiMineInfo hiMineInfo);
	
	/**
	 * 批量插入hiMineInfo
	 * @param listHiMineInfo
	 */
	public void insertGroupHi(List<HiMineInfo> listHiMineInfo);
	
}