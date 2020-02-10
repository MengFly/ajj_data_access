package com.akxy.service;

import com.akxy.entity.*;

import java.util.List;

public interface ILocalCacheService {

    /**
     * 准备读取中间库缓存信息
     * 这里的缓存每一次处理都要重新读取一次
     */
    void prepareMidCache(String primaryDb, String mineDb);

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
