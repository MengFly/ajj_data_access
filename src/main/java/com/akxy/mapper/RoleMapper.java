package com.akxy.mapper;

import com.akxy.entity.Role;

public interface RoleMapper {
    int deleteByPrimaryKey(Short roleId);

    int insert(Role record);

    int insertSelective(Role record);

    Role selectByPrimaryKey(Short roleId);

    int updateByPrimaryKeySelective(Role record);

    int updateByPrimaryKey(Role record);
}