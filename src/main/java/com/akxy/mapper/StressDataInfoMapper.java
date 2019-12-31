package com.akxy.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.StressDataInfo;

@Mapper
public interface StressDataInfoMapper {

    /**
     * 最新数据上传时间
     *
     * @return
     */
    Date greatestNewTime();

    /**
     * 判断是否存在主键
     *
     * @return
     */
    List<String> findConditions();

    /**
     * 将组装好的数据写入STRESS_DATAINFO
     * @return 1为写入成功 0为写入失败
     */
    Integer writeToDataInfo(StressDataInfo stressDataInfo);

    /**
     * 判断STRESS_DATAINFO是否已经写入过该数据
     *
     * @return
     */
    Integer judgeContain(@Param("acquisitionTime") Date acquisitionTime, @Param("mpId") Long mpId,
                         @Param("areaId") Long areaId);

    /**
     * 根据测点ID得到该测点出现时间的降序集合
     */
    List<Date> getPointTimeByID(@Param("mpId") Long mpId);

    void deleteDataInfos(List<StressDataInfo> list);

    /**
     * 批量插入STRESS_DATAINFO数据
     */
    void insertGroupDataInfo(List<StressDataInfo> listStressDataInfos);

    /**
     * 根据AreaId和MpId获取按时间降序的第一行
     */
    StressDataInfo getDataInfos(@Param("areaId") Long areaId, @Param("mpId") Long mpId);

    /**
     * 获取最新一天的StressDataInfo信息
     */
    List<StressDataInfo> getDataInfoCache();

}