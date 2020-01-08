package com.akxy.mapper;


import com.akxy.entity.Area;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
     */
    List<Area> getArea();

}