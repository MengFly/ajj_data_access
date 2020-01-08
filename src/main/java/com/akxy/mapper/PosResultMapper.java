package com.akxy.mapper;

import com.akxy.entity.PosResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface PosResultMapper {

	Date findNewDate();

	/**
	 * 批量插入POSRESULT
	 */
	void insertGroupData(List<PosResult> posResults);

}