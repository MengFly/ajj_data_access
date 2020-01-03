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

@SuppressWarnings("unchecked")
@Service
public class LocalCacheServiceImpl implements ILocalCacheService {

    // 子矿区数据库中的信息，不需要每次都从数据库中取出
    private Map<String, List<Area>> totalMineAreaCache = new ConcurrentHashMap<>();
    private Map<String, List<StressMeasurePoint>> totalPointCache = new ConcurrentHashMap<>();
    private Map<String, List<ConnTopStatus>> totalConnTopCache = new ConcurrentHashMap<>();
    private Map<String, List<CurMineInfo>> totalCurMineCache = new ConcurrentHashMap<>();
    private Map<String, List<StressDataInfo>> totalStressDataCache = new ConcurrentHashMap<>();


    // 中间库缓存，每一次读取都需要重新读取，因此放在同一个列表里面，方便清理
    private Map<String, Object> midDataBaseCache = new ConcurrentHashMap<>();


    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private StressMeasurePointMapper measurePointMapper;
    @Autowired
    private ConnTopStatusMapper connTopStatusMapper;
    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;
    @Autowired
    private MineMapper mineMapper;
    @Autowired
    private CurMineInfoMapper curMineInfoMapper;
    @Autowired
    StressDataInfoMapper stressDataInfoMapper;


    @Override
    public List<Area> getMineAreaCache(String mineDb) {
        if (!totalMineAreaCache.containsKey(mineDb)) {
            resetMineAreaCache(mineDb);
        }
        List<Area> areas = totalMineAreaCache.getOrDefault(mineDb, null);
        if (areas == null) {
            resetMineAreaCache(mineDb);
            return totalMineAreaCache.getOrDefault(mineDb, Collections.emptyList());
        } else {
            return areas;
        }
    }

    @Override
    public void resetMineAreaCache(String mineDb) {
        DynamicDataSourceContextHolder.setDataSource(mineDb);
        totalMineAreaCache.put(mineDb, areaMapper.getArea());
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
    public List<ConnTopStatus> getMineConnTopStatusCache(String mineDb) {
        if (!totalConnTopCache.containsKey(mineDb)) {
            resetMineConnTopStatusCache(mineDb);
        }
        List<ConnTopStatus> connTopStatuses = totalConnTopCache.getOrDefault(mineDb, null);
        if (connTopStatuses == null) {
            resetMineConnTopStatusCache(mineDb);
            return totalConnTopCache.getOrDefault(mineDb, Collections.emptyList());
        } else {
            return connTopStatuses;
        }
    }

    @Override
    public void resetMineConnTopStatusCache(String mineDb) {
        DynamicDataSourceContextHolder.setDataSource("1000"); // 安监局平台数据库
        totalConnTopCache.put(mineDb, connTopStatusMapper.getTopStatusByMineCode(mineDb));
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public List<CurMineInfo> getCurMineCache(String mineDb) {
        if (!totalCurMineCache.containsKey(mineDb)) {
            resetCurMineCache(mineDb);
        }
        List<CurMineInfo> curMine = totalCurMineCache.getOrDefault(mineDb, null);
        if (curMine == null) {
            resetCurMineCache(mineDb);
            return totalCurMineCache.getOrDefault(mineDb, Collections.emptyList());
        } else {
            return curMine;
        }
    }

    @Override
    public void resetCurMineCache(String mineDb) {
        DynamicDataSourceContextHolder.setDataSource(mineDb);
        totalCurMineCache.put(mineDb, curMineInfoMapper.getAllCurMine());
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public void prepareMidCache(String primaryDB, String mineDb) {
        DynamicDataSourceContextHolder.setDataSource(primaryDB);// 中间库数据库
        midDataBaseCache.put("STRESS" + mineDb, stressMapper.readStressData(mineDb));// 1000 条应力数据
        midDataBaseCache.put("QUAKE" + mineDb, quakeMapper.readQuakeData(mineDb)); // 1000 条微震信息
        midDataBaseCache.put("MINE" + mineDb, mineMapper.listMines(mineDb));// 本矿区信息
        midDataBaseCache.put("AREANAME" + mineDb, stressMapper.getAllAreaName(mineDb));// 应力数据中包含的工作面信息
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
        return orDefault == null ? Collections.emptyList() : (List<String>) orDefault;
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
