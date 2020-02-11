package com.akxy.service;

import java.util.List;

/**
 * @author wangp
 */
public interface IDataAccessService {


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
     * 写入PLATFORM连接状态表
     */
    void writeToPlatform(String primaryDb, String customDb, String mineName);

    boolean hasNeedAnalysisData();

    void updateMpIdAndAreaId(List<String> childMines);
}
