package com.akxy.mapper;


import com.akxy.entity.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wangp
 */
@Mapper
public interface AreaMapper {

    /**
     * 向Area表中添加数据
     *
     * @param area 数据的entity
     * @return 返回1：插入成功；返回0：插入失败
     */
    int insertData(Area area);

    /**
     * 获取Area列表
     *
     * @return 获取所有工作面信息，以id逆向排序
     */
    List<Area> getArea();

}