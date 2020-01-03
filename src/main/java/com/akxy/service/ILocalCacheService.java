package com.akxy.service;

import com.akxy.entity.*;

import java.util.List;
import java.util.UUID;

public interface ILocalCacheService {

    /**
     * 获取到子矿区中的工作面缓存，并且结果根据Area.id进行排序
     */
    List<Area> getMineAreaCache(String mineDb);

    /**
     * 重载子矿区工作面缓存
     */
    void resetMineAreaCache(String mineDb);


    /**
     * 获取到子矿区中的所有测点缓存
     */
    List<StressMeasurePoint> getMinePointCache(String mineDb);

    /**
     * 重载子矿区测点缓存
     */
    void resetMinePointCache(String mineDb);


    /**
     * 获取子矿区最新的1000条应力信息，这个用来和中间库的应力信息进行比对
     */
    List<StressDataInfo> getStressDataCache(String mineDb);

    /**
     * 重载子矿区的最新应力缓存信息
     */
    void resetStressDataCache(String mineDb);

    /**
     * 获取平台数据库个子矿区连接状态信息
     */
    List<ConnTopStatus> getMineConnTopStatusCache(String mineDb);

    /**
     * 重载平台连接状态缓存
     */
    void resetMineConnTopStatusCache(String mineDb);


    List<CurMineInfo> getCurMineCache(String mineDb);

    void resetCurMineCache(String mineDb);

    /**
     * 准备读取中间库缓存信息
     * 这里的缓存每一次处理都要重新读取一次
     */
    void prepareMidCache(String primaryDB, String mineDb);

    /**
     * 清理中间库缓存信息
     * 在所有矿区信息处理完成之后要进行清理，释放空间
     */
    void restoreAllMidCache();

    /**
     * 获取中间库缓存的应力信息
     */
    List<Stress> getMidStressCache(String mineDb);

    /**
     * 获取中间库缓存的微震信息
     */
    List<Quake> getMidQuakeCache(String mineDb);

    /**
     * 获取中间库缓存的矿区信息
     */
    List<Mine> getMidMineCache(String mineDb);

    /**
     * 获取中间库缓存的工作面名称信息
     */
    List<String> getMidAreaNameCache(String mineDb);

    /**
     * 获取子矿区名称
     */
    String getMineName(String mineDb);
}
