package com.akxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrganMineMapper {

    /**
     * 根据矿名查看ID
     */
    String findIDByName(@Param("name") String name);
}