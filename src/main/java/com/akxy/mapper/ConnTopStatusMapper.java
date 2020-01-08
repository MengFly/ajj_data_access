package com.akxy.mapper;

import com.akxy.entity.ConnTopStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConnTopStatusMapper {
	
	/**
	 * 更新
	 */
	 void updateConnTop(ConnTopStatus connTopStatus);

	 List<ConnTopStatus> getTopStatusByMineCode(@Param("mineCode") String mineCode);
	
	/**
	 * 批量插入CONN_TOP_STATUS
	 */
	 void insertGroupData(List<ConnTopStatus> connTopStatus);
}