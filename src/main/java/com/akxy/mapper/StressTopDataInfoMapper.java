package com.akxy.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.StressTopDataInfo;

@Mapper
public interface StressTopDataInfoMapper {

    Date findNewDate();

    List<StressTopDataInfo> stressTopDataInfos();

    /**
     * 批量插入Stress_Top_DataInfo
     *
     * @param listTopStress 要存入的数据
     * @return 成功存入的个数
     */
    int insertGroupData(List<StressTopDataInfo> listTopStress);

    /**
     * 更新Top表
     */
    void updateTopData(StressTopDataInfo stressTopDataInfo);

    /**
     * 根据工作面Id和测点Id查询top表数据个数
     *
     * @param areaId 工作面Id
     * @param mpId   测点Id
     * @return 个数
     */
    int countBy(@Param("areaId") Long areaId, @Param("mpId") Long mpId);
}