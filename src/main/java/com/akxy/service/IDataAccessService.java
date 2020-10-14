package com.akxy.service;

import com.akxy.entity.Quake;
import com.akxy.entity.Stress;

import java.util.List;
import java.util.Set;

/**
 * @author wangp
 */
public interface IDataAccessService {


    /**
     * 从数据库读取,计算Stress数据
     */
    void readAndCalculateStress(String primaryDb, String customDb, List<Stress> stresses);

    /**
     * 从数据库读取,计算Quake数据
     */
    void readAndCalculateQuake(String primaryDb, String customDb, List<Quake> quakes);

    /**
     * 写入测点
     */
    void writeNotExistsMeasurePoint(String customDb, List<Stress> stresses);

    /**
     * 配置Area(工作面)表
     */
    void configArea(String primaryDb, String customDb, Set<String> areaNames);

    /**
     * 写入PLATFORM连接状态表
     */
    void writeToPlatform(String primaryDb, String customDb);
}
