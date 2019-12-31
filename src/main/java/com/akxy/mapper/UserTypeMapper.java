package com.akxy.mapper;

import com.akxy.entity.UserType;

public interface UserTypeMapper {
    int deleteByPrimaryKey(Short userTypeId);

    int insert(UserType record);

    int insertSelective(UserType record);

    UserType selectByPrimaryKey(Short userTypeId);

    int updateByPrimaryKeySelective(UserType record);

    int updateByPrimaryKey(UserType record);
}