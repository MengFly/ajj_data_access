package com.akxy.mapper;

import com.akxy.entity.Stress;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface StressMapper {

    /**
     * 批量删除已读数据
     *
     * @param listStress
     */
    int deleteGroupData(List<Stress> listStress);

    List<Stress> readByRowNumTimeAsc(@Param("rowNum") int rowNum,@Param("rowNumEnd") int rowNumEnd);
}