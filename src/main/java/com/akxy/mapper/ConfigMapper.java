package com.akxy.mapper;

import com.akxy.entity.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConfigMapper {

    /**
     * 获取配置信息
     */
    Config getConfigInfo(@Param("type") String type, @Param("name") String name);

}