package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.StressMeasurePoint;

@Mapper
public interface StressMeasurePointMapper {

    /**
     * 获取该数据对应的MP_ID
     *
     * @param tunnelName 巷道名称
     * @param distance   距离
     * @param depth      深度
     */
    Long getMPID(@Param("tunnelName") String tunnelName, @Param("depth") Double depth,
                 @Param("distance") Double distance);

    /**
     * 插入测点
     *
     * @param stressMeasurePoint 新建测点的数据
     * @return 1 写入成功 0 写入失败
     */
    Integer writeMeasurePoint(StressMeasurePoint stressMeasurePoint);

    /**
     * 得到所有测点信息
     */
    List<StressMeasurePoint> getAllPoint();

    /**
     * 批量更新STRESS_MEASUREPOINT
     */
    void updateGroupData(List<StressMeasurePoint> list);

}