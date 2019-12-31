package com.akxy.mapper;

import com.akxy.entity.OperateLog;

public interface OperateLogMapper {
    int insert(OperateLog record);

    int insertSelective(OperateLog record);
}