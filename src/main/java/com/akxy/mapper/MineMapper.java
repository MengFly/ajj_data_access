package com.akxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MineMapper {

    String findNameByCode(@Param("mineCode") String mineCode);

}