package com.akxy.service;

import java.util.List;

import com.akxy.entity.Area;
import com.akxy.entity.Stress;
import com.akxy.entity.StressMeasurePoint;

public interface IDataAccessService {

    /**
     * copy中间库到本地
     */
    void copyDBToLocal(String customDB, String mineName);

    /**
     * 从数据库读取,计算Stress数据
     */
    void readAndCalculateStress(String primaryDB, String customDB, String mineName);

    /**
     * 从数据库读取,计算Quake数据
     */
    void readAndCalculateQuake(String primaryDB, String customDB, String mineName);


    /**
     * 把当前预警数据写入CURMINE_INFO
     */
    void writeToCurMine(String primaryDB, String customDB, List<Stress> listStress);

    /**
     * 写入历史预警数据HIMINE_INFO
     */
    void writeToHiMine(String customDB, List<Area> areas, List<StressMeasurePoint> stressMeasurePoints, List<Stress> stresses);

    /**
     * 写入测点
     */
    void writeNotExistsMeasurePoint(String customDB, String mineName);

    /**
     * 配置Area(工作面)表
     */
    void configArea(String primaryDB, String customDB, String mineName);

    /**
     * 更新STRESS_MEASUREPOINT FROM_TIME & TO_TIME
     */
    void updatePointTime(String primaryDB, String customDB, String mineName);

    /**
     * 写入PLATFORM连接状态表
     */
    void writeToPlatform(String primaryDB, String customDB, String mineName);

    boolean hasNeedAnalysisData();

}
