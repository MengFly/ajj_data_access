package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.akxy.entity.ConnStatus;

@Mapper
public interface ConnStatusMapper {
	
	/**
	 * 批量插入CONN_STATUS
	 * @param list
	 */
	public void insertGroupData(List<ConnStatus> connStatus);
	
    int insert(ConnStatus record);

    int insertSelective(ConnStatus record);
}