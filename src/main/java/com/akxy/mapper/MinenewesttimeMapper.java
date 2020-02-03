package com.akxy.mapper;

import com.akxy.entity.MinenewestTime;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MinenewesttimeMapper {

    int insert(MinenewestTime record);

    MinenewestTime selectByPrimaryKey(String mineCode);

    int updateByPrimaryKey(MinenewestTime record);

    List<MinenewestTime> selectAll();
}