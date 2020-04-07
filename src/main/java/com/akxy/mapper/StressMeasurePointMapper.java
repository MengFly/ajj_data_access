package com.akxy.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.StressMeasurePoint;

@Mapper
public interface StressMeasurePointMapper {

    /**
     * 获取该数据对应的MP_ID
     *
     * @param areaId     工作面Id
     * @param tunnelName 巷道名称
     * @param distance   距离
     * @param depth      深度
     */
    Long findIdBy(@Param("areaId") Long areaId,
                  @Param("tunnelName") String tunnelName,
                  @Param("depth") Double depth,
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

    int countBy(@Param("areaId") Long areaId,
                @Param("tunnelName") String tunnelName,
                @Param("distance") Double distance,
                @Param("depth") Double depth);

    Long findMaxId();

    void updateToTime(ArrayList<StressMeasurePoint> stressMeasurePoints);

    void updateFromTime(ArrayList<StressMeasurePoint> stressMeasurePoints);

    void deleteById(@Param("id") Long id);

    void updateMpName(ArrayList<StressMeasurePoint> stressMeasurePoints);
}