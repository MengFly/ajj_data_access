package com.akxy.mapper;

import com.akxy.entity.MineInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HiMineInfoMapper {

	/**
	 * 批量插入hiMineInfo
	 */
	void insertGroupHi(List<MineInfo> listHiMineInfo);
	
}