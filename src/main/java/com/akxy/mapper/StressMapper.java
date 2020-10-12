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
     * 从中间库读取Stress数据
     *
     * @return 返回Stress数据组成的List集合
     */
    List<Stress> readStressData(@Param("customDB") String customdB);

    /**
     * 批量删除已读数据
     *
     * @param listStress
     */
    int deleteGroupData(List<Stress> listStress);

    Integer stressCount();

    List<Stress> readJustByTime(@Param("time") String formatDate, @Param("count") int readStressCount);
}