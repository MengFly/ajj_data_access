package com.akxy.mapper;

import com.akxy.entity.PosResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface PosResultMapper {

    Date findNewDate();

    /**
     * 批量插入POSRESULT
     */
    int insertGroupData(List<PosResult> posResults);

    int count(@Param("item") PosResult posResult);
}