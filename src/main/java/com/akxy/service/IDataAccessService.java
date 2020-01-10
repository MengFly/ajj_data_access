package com.akxy.service;

/**
 * @author wangp
 */
public interface IDataAccessService {

    /**
     * copy中间库到本地
     */
    void copyDbToLocal(String customDb, String mineName);

    /**
     * 从数据库读取,计算Stress数据
     */
    void readAndCalculateStress(String primaryDb, String customDb, String mineName);

    /**
     * 从数据库读取,计算Quake数据
     */
    void readAndCalculateQuake(String primaryDb, String customDb, String mineName);

    /**
     * 写入测点
     */
    void writeNotExistsMeasurePoint(String customDb, String mineName);

    /**
     * 配置Area(工作面)表
     */
    void configArea(String primaryDb, String customDb, String mineName);

    /**
     * 更新STRESS_MEASUREPOINT FROM_TIME & TO_TIME
     */
    void updatePointTime(String primaryDb, String customDb, String mineName);

    /**
     * 写入PLATFORM连接状态表
     */
    void writeToPlatform(String primaryDb, String customDb, String mineName);

    boolean hasNeedAnalysisData();

}
