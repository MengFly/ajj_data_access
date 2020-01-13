package com.akxy.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author wangp
 */
@Mapper
public interface AreaTopDataInfoMapper {

    /**
     * 获取全矿预警
     * @return 全矿预警
     */
    Short findAreaValue();

}