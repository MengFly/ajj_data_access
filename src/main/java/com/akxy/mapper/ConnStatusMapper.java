package com.akxy.mapper;

import com.akxy.entity.ConnStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConnStatusMapper {

    /**
     * 批量插入CONN_STATUS
     */
    void insertGroupData(List<ConnStatus> connStatus);
}