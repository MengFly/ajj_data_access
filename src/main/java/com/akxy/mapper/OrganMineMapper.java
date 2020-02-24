package com.akxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrganMineMapper {

    String findNameById(@Param("id") String id);
}