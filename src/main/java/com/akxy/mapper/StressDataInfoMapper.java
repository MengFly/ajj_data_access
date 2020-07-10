package com.akxy.mapper;

import com.akxy.entity.StressDataInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author wangp
 */
@Mapper
public interface StressDataInfoMapper {

    /**
     * 批量插入STRESS_DATAINFO数据
     *
     * @param listStressDataInfos 需要存入的应力数据列表
     * @return 存储成功的个数
     */
    int insertGroupDataInfo(List<StressDataInfo> listStressDataInfos);

}