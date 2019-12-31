package com.akxy.mapper;

import com.akxy.entity.UserRole;

public interface UserRoleMapper {
    int deleteByPrimaryKey(Short id);

    int insert(UserRole record);

    int insertSelective(UserRole record);

    UserRole selectByPrimaryKey(Short id);

    int updateByPrimaryKeySelective(UserRole record);

    int updateByPrimaryKey(UserRole record);
}