package com.akxy.service.impl;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.*;
import com.akxy.service.ILocalCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author wangp
 */
@SuppressWarnings("unchecked")
@Service
public class LocalCacheServiceImpl implements ILocalCacheService {

    // 中间库缓存，每一次读取都需要重新读取，因此放在同一个列表里面，方便清理
    private Map<String, Object> midDataBaseCache = new ConcurrentHashMap<>();

    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;
    @Autowired
    private MineMapper mineMapper;
    @Autowired
    StressDataInfoMapper stressDataInfoMapper;

    @Override
    public void prepareMidCache(String primaryDb, String mineDb) {
        // 中间库数据库
        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        // 1000 条应力数据
        midDataBaseCache.put("STRESS" + mineDb, stressMapper.readStressData(mineDb));
        // 1000 条微震信息
        midDataBaseCache.put("QUAKE" + mineDb, quakeMapper.readQuakeData(mineDb));
        // 本矿区信息
        midDataBaseCache.put("MINE" + mineDb, mineMapper.listMines(mineDb));
        // 应力数据中包含的工作面信息
        midDataBaseCache.put("AREANAME" + mineDb, stressMapper.getAllAreaName(mineDb));
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public List<Stress> getMidStressCache(String mineDb) {
        Object orDefault = midDataBaseCache.getOrDefault("STRESS" + mineDb, null);
        return orDefault == null ? Collections.emptyList() : (List<Stress>) orDefault;
    }

    @Override
    public List<Quake> getMidQuakeCache(String mineDb) {
        Object orDefault = midDataBaseCache.getOrDefault("QUAKE" + mineDb, null);
        return orDefault == null ? Collections.emptyList() : (List<Quake>) orDefault;
    }

    @Override
    public List<Mine> getMidMineCache(String mineDb) {
        Object orDefault = midDataBaseCache.getOrDefault("MINE" + mineDb, null);
        return orDefault == null ? Collections.emptyList() : (List<Mine>) orDefault;
    }

    @Override
    public List<String> getMidAreaNameCache(String mineDb) {
        Object orDefault = midDataBaseCache.getOrDefault("AREANAME" + mineDb, null);
        return orDefault == null ? Collections.emptyList() :
                ((List<String>) orDefault).stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void restoreAllMidCache() {
        midDataBaseCache.clear();
    }

    @Override
    public String getMineName(String mineDb) {
        List<Mine> curMineCache = getMidMineCache(mineDb);
        if (curMineCache.isEmpty()) {
            return "UNKNOWN";
        } else {
            return curMineCache.get(0).getName();
        }
    }
}
