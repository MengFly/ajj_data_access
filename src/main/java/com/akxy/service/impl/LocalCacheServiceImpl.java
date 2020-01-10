package com.akxy.service.impl;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.*;
import com.akxy.mapper.*;
import com.akxy.service.ILocalCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
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

    /**
     * 子矿区数据库中的信息，不需要每次都从数据库中取出
     */
    private Map<String, Map<String, Area>> totalMineAreaCache = new ConcurrentHashMap<>();
    private Map<String, List<StressMeasurePoint>> totalPointCache = new ConcurrentHashMap<>();
    private Map<String, List<StressDataInfo>> totalStressDataCache = new ConcurrentHashMap<>();


    // 中间库缓存，每一次读取都需要重新读取，因此放在同一个列表里面，方便清理
    private Map<String, Object> midDataBaseCache = new ConcurrentHashMap<>();


    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private StressMeasurePointMapper measurePointMapper;
    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;
    @Autowired
    private MineMapper mineMapper;
    @Autowired
    StressDataInfoMapper stressDataInfoMapper;

    @Override
    public Map<String, Area> getMineAreaCache(String mineDb) {
        if (!totalMineAreaCache.containsKey(mineDb)) {
            resetMineAreaCache(mineDb);
        }
        Map<String, Area> areas = totalMineAreaCache.getOrDefault(mineDb, null);
        if (areas == null) {
            resetMineAreaCache(mineDb);
            return totalMineAreaCache.getOrDefault(mineDb, Collections.emptyMap());
        } else {
            return areas;
        }
    }

    @Override
    public void resetMineAreaCache(String mineDb) {
        DynamicDataSourceContextHolder.setDataSource(mineDb);
        Map<String, Area> areaMap = new LinkedHashMap<>();
        for (Area area : areaMapper.getArea()) {
            areaMap.put(area.getName(), area);
        }
        totalMineAreaCache.put(mineDb, areaMap);
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public List<StressMeasurePoint> getMinePointCache(String mineDb) {
        if (!totalPointCache.containsKey(mineDb)) {
            resetMinePointCache(mineDb);
        }
        List<StressMeasurePoint> points = totalPointCache.getOrDefault(mineDb, null);
        if (points == null) {
            resetMinePointCache(mineDb);
            return totalPointCache.getOrDefault(mineDb, Collections.emptyList());
        } else {
            return points;
        }
    }

    @Override
    public void resetMinePointCache(String mineDb) {
        DynamicDataSourceContextHolder.setDataSource(mineDb);
        totalPointCache.put(mineDb, measurePointMapper.getAllPoint());
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public List<StressDataInfo> getStressDataCache(String mineDb) {
        if (!totalStressDataCache.containsKey(mineDb)) {
            resetMinePointCache(mineDb);
        }
        List<StressDataInfo> stressDataInfos = totalStressDataCache.getOrDefault(mineDb, null);
        if (stressDataInfos == null) {
            resetStressDataCache(mineDb);
            return totalStressDataCache.getOrDefault(mineDb, Collections.emptyList());
        } else {
            return stressDataInfos;
        }

    }

    @Override
    public void resetStressDataCache(String mineDb) {
        DynamicDataSourceContextHolder.setDataSource(mineDb);
        totalStressDataCache.put(mineDb, stressDataInfoMapper.getDataInfoCache());
        DynamicDataSourceContextHolder.restoreDataSource();
    }

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
