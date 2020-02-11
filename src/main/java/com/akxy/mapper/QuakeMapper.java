package com.akxy.mapper;

import com.akxy.entity.Quake;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface QuakeMapper {

    /**
     * 批量删除已读微震
     */
    int deleteGroupData(List<Quake> quakes);

    /**
     * 从中间库读取Quake数据
     *
     * @return 返回Quake数据组成的List集合
     */
    List<Quake> readQuakeData(@Param("customDB") String customDB);

    Integer quakeCount();

    int deleteByTimeLessThan(@Param("time") Timestamp time);

}