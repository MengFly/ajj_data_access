package com.akxy.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AreaTopDataInfoMapper {

    /**
     * 获取全矿预警
     */
    Short findAreaValue();

}