package com.akxy.mapper;

import com.akxy.entity.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author wangp
 */
@Mapper
public interface ConfigMapper {

    /**
     * 获取配置信息
     *
     * @param type 配置类型
     * @param name 配置名称
     * @return 配置对象
     */
    Config getConfigInfo(@Param("type") String type, @Param("name") String name);

}