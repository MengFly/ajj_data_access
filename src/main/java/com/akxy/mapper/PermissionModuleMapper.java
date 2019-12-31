package com.akxy.mapper;

import com.akxy.entity.PermissionModule;

public interface PermissionModuleMapper {
    int deleteByPrimaryKey(Short permId);

    int insert(PermissionModule record);

    int insertSelective(PermissionModule record);

    PermissionModule selectByPrimaryKey(Short permId);

    int updateByPrimaryKeySelective(PermissionModule record);

    int updateByPrimaryKey(PermissionModule record);
}