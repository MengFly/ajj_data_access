package com.akxy.mapper;

import com.akxy.entity.Permission;

public interface PermissionMapper {
    int deleteByPrimaryKey(Short permId);

    int insert(Permission record);

    int insertSelective(Permission record);

    Permission selectByPrimaryKey(Short permId);

    int updateByPrimaryKeySelective(Permission record);

    int updateByPrimaryKey(Permission record);
}