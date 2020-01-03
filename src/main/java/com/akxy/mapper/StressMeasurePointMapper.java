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

    List<Long> getMPIDList(@Param("tunnelName") String tunnelName, @Param("depth") Double depth,
                           @Param("distance") Double distance);

    /**
     * 根据测点标志获取MP_ID
     */
    Long getMPIDByPostSign(@Param("tunnelName") String tunnelName, @Param("depth") Float depth,
                           @Param("distance") Float distance);

    /**
     * 插入测点
     *
     * @param stressMeasurePoint 新建测点的数据
     * @return 1 写入成功 0 写入失败
     */
    Integer writeMeasurePoint(StressMeasurePoint stressMeasurePoint);

    /**
     * 获取STRESS_MEASUREPOINT表中所有的MP_ID
     *
     * @return MP_ID组成的降序集合
     */
    List<Long> getMPIDSDESC();

    /**
     * 判断测点表中是否已经录入该测点
     */
    Integer judgeContain(@Param("tunnelName") String tunnelName, @Param("depth") Double depth,
                         @Param("distance") Double distance);

    /**
     * 根据公司标志对ID进行查询并降序返回
     */
    List<Long> getFuzzyQueryID(@Param("sign") String sign);

    /**
     * 更新测点信息
     */
    void updateMessurePoint(StressMeasurePoint stressMeasurePoint);

    /**
     * 得到所有测点信息
     */
    List<StressMeasurePoint> getAllPoint();

    /**
     * 批量插入STRESS_MEASUREPOINT
     */
    void insertGroupData(List<StressMeasurePoint> list);

    /**
     * 批量更新STRESS_MEASUREPOINT
     */
    void updateGroupData(List<StressMeasurePoint> list);

}