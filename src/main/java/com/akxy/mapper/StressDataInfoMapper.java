package com.akxy.mapper;

import com.akxy.entity.StressDataInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author wangp
 */
@Mapper
public interface StressDataInfoMapper {

    /**
     * 批量插入STRESS_DATAINFO数据
     * @param listStressDataInfos 需要存入的应力数据列表
     * @return 存储成功的个数
     */
    int insertGroupDataInfo(List<StressDataInfo> listStressDataInfos);

    /**
     * 获取最新一天的StressDataInfo信息
     */
    List<StressDataInfo> getDataInfoCache();

    /**
     * 获取子矿区应力历史表中的最大时间
     *
     * @return 最大时间
     */
    Timestamp findMaxAcquisitionTime();

    /**
     * 根据工作面Id和测点Id查询最大时间
     *
     * @param areaId 工作面id
     * @param mpId   测点id
     * @return 最大时间
     */
    Timestamp findMaxAcquisitionTimeBy(@Param("areaId") Long areaId, @Param("mpId") Long mpId);

    int updateAreaIdByMpId(@Param("mpId") Long mpId,@Param("areaId") Long areaId);

    int countBy(@Param("mpId") Long mpId);

    int updateMpNameByMpId(@Param("mpId") Long id,@Param("mpName") String name);
}