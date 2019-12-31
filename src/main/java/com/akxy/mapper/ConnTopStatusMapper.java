package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.akxy.entity.ConnTopStatus;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConnTopStatusMapper {
	
	/**
	 * 更新
	 * @param connTopStatus
	 */
	 void updateConnTop(ConnTopStatus connTopStatus);

	 List<ConnTopStatus> getTopStatusByMineCode(@Param("mineCode") String mineCode);
	
	/**
	 * 批量插入CONN_TOP_STATUS
	 */
	 void insertGroupData(List<ConnTopStatus> connTopStatus);
	
	/**
	 * 批量更新CONN_TOP_STATUS
	 * @param connTopStatus
	 */
	public void updateGroupData(List<ConnTopStatus> connTopStatus);
	
    int insert(ConnTopStatus record);

    int insertSelective(ConnTopStatus record);
}