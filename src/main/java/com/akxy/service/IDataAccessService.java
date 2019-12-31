package com.akxy.service;

import java.util.List;
import java.util.Map;

import com.akxy.entity.Area;
import com.akxy.entity.Stress;

public interface IDataAccessService {

    /**
     * copy中间库到本地
     */
    void copyDBToLocal(String customDB);

    /**
     * 从数据库读取,计算Stress数据
     *
     * @return 处理完成并组装好的数据
     */
    void readAndCalculateStress(String primaryDB, String customDB);

    /**
     * 从数据库读取,计算Quake数据
     */
    void readAndCalculateQuake(String primaryDB, String customDB);


    /**
     * 把当前预警数据写入CURMINE_INFO
     */
    void writeToCurMine(String primaryDB, String customDB, List<Stress> listStress);

    /**
     * 写入历史预警数据HIMINE_INFO
     */
    void writeToHiMine(String primaryDB, String customDB, List<Stress> stresses);

    /**
     * 写入测点
     */
    void writeToMeasurePoint(String primaryDB, String customDB);

    /**
     * 配置Area表
     */
    void configArea(String primaryDB, String customDB);

    /**
     * 更新STRESS_MEASUREPOINT FROM_TIME & TO_TIME
     */
    void updatePointTime(String primaryDB, String customDB);

    /**
     * 获取缓存
     *
     * @return
     */
    Map<String, Object> getMapCache(String primaryDB, String customDB);

    /**
     * 写入PLATFORM连接状态表
     */
    void writeToPlatform(String primaryDB, String customDB);

    boolean hasNeedAnalysisData();

}
