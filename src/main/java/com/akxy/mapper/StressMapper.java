package com.akxy.mapper;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.PointSign;
import com.akxy.entity.Stress;

@Mapper
public interface StressMapper {

    /**
     * 从中间库读取Stress数据
     *
     * @return 返回Stress数据组成的List集合
     */
    List<Stress> readStressData(@Param("customDB") String customDB);

    /**
     * 根据去重的标志获取Top表所需测点
     *
     * @param tunnelName 巷道名
     * @param depth      深度
     * @param distance   距离
     * @return
     */
    Stress getDistinctPoint(@Param("customDB") String customDB, @Param("tunnelName") String tunnelName,
                            @Param("depth") Double depth, @Param("distance") Double distance);

    /**
     * 获取STRESS、QUAKE表中所有的AreaName
     *
     * @return
     */
    List<String> getAllAreaName(@Param("mineCode") String mineCode);

    /**
     * 批量删除已读数据
     *
     * @param listStress
     */
    int deleteGroupData(List<Stress> listStress);

    Integer stressCount();

    int deleteByTimeLessThan(@Param("time") Timestamp time);
}