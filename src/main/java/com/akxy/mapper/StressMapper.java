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
     * 从中间库获取每个测点标志
     *
     * @return
     */
    List<PointSign> getPointSignList(@Param("customDB") String customDB);

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
     * 取STRESS表每个测点最新的预警信息
     *
     * @param tunnelName 巷道名
     * @param depth      深度
     * @param distance   距离
     * @return
     */
    Stress getCurWarnStress(@Param("tunnelName") String tunnelName, @Param("depth") Double depth,
                            @Param("distance") Double distance);

    /**
     * 获取所有预警数据
     *
     * @return
     */
    List<Stress> getAllWarnStress();

    /**
     * 获取位置不重复的应力信息
     *
     * @return
     */
    List<Stress> getDistince();

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
    void deleteGtoupData(List<Stress> listStress);

    /**
     * 删除所有Stress数据
     */
    void truncateAll();

    /**
     * 根据AreaName获取最大value值
     *
     * @param areaName
     * @return
     */
    Double getValueByName(@Param("areaName") String areaName);

    Integer stressCount();

    int deleteByTimeLessThan(@Param("time") Timestamp time);
}