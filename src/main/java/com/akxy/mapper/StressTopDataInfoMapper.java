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
     */
    void insertGroupData(List<StressTopDataInfo> listTopStress);

    /**
     * 更新Top表
     */
    void updateTopData(StressTopDataInfo stressTopDataInfo);

}