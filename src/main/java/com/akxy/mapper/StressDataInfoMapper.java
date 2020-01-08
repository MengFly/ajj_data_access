package com.akxy.mapper;

import com.akxy.entity.StressDataInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StressDataInfoMapper {

    void deleteDataInfos(List<StressDataInfo> list);

    /**
     * 批量插入STRESS_DATAINFO数据
     */
    void insertGroupDataInfo(List<StressDataInfo> listStressDataInfos);

    /**
     * 获取最新一天的StressDataInfo信息
     */
    List<StressDataInfo> getDataInfoCache();
}