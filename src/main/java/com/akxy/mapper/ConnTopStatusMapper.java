package com.akxy.mapper;

import com.akxy.entity.ConnTopStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConnTopStatusMapper {
	
    void update(ConnTopStatus connTopStatus);

    int insert(ConnTopStatus topStatus);

    int countByMineCodeAndType(@Param("topStatus") ConnTopStatus topStatus);

}