package com.akxy.service.impl;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.Quake;
import com.akxy.entity.Stress;
import com.akxy.mapper.MineMapper;
import com.akxy.mapper.QuakeMapper;
import com.akxy.mapper.StressMapper;
import com.akxy.service.ILocalCacheService;
import com.akxy.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wangp
 */
@SuppressWarnings("unchecked")
@Service
@Slf4j
public class LocalCacheServiceImpl implements ILocalCacheService {
    /**
     * 中间库缓存，每一次读取都需要重新读取，因此放在同一个列表里面，方便清理
     */
    private Map<String, Object> midDataBaseCache = new ConcurrentHashMap<>();
    /**
     * 存储每个矿的最早的时间
     */
    private Map<String, Date> midStressMinDate = new ConcurrentHashMap<>();
    /**
     * 存储每个矿的查询耗时
     */
    private Map<String, Long> midStressReadCost = new ConcurrentHashMap<>();

    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;
    @Autowired
    private MineMapper mineMapper;

    @Value("${custom.datasource.names}")
    public String customDbs;

    public int readStressCount = -1;

    @Override
    public void prepareMidCache(String primaryDb, String mineDb) {
        // 中间库数据库
        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        // 1000 条应力数据
        List<Stress> stressList = readStressData(mineDb);
        midDataBaseCache.put("STRESS" + mineDb, stressList);
        // 1000 条微震信息
        List<Quake> quakeList = quakeMapper.readQuakeData(mineDb);
        midDataBaseCache.put("QUAKE" + mineDb, quakeList);
        // 应力数据中包含的工作面信息
        Set<String> areaList = stressList.stream().map(Stress::getAreaname).parallel().collect(Collectors.toSet());
        areaList.addAll(quakeList.stream().map(Quake::getAreaname).parallel().distinct().collect(Collectors.toList()));

        midDataBaseCache.put("AREANAME" + mineDb, new ArrayList<>(areaList));
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    /**
     * 读取矿区应力数据
     *
     * @param mineDb MineCode
     * @return 应力数据
     */
    private List<Stress> readStressData(String mineDb) {
        // initReadStressCount, count need between 1000 and 4000
        if (readStressCount == -1) {
            if (customDbs != null) {
                readStressCount = Math.max((customDbs.split(",").length - 2) * 1000, 1000);
                readStressCount = Math.min(4000, readStressCount);
            } else {
                readStressCount = 1000;
            }
            log.info("init readStressCount ==> {}", readStressCount);
        }

        List<Stress> resultStress;

        Long midCost = midStressReadCost.getOrDefault(mineDb, null);
        Date maxTime = midStressMinDate.getOrDefault(mineDb, null);
        // 假设快速读取的平均速度为5秒一次，那么要保证平均的处理速度在30秒一次
        int lucky;
        if (midCost == null) {
            lucky = 0;
        } else {
            // 计算方式 lastReadCost:midCost / (Except:30 - mean(fastRead):5)
            lucky = new Random().nextInt(1 + (int) (midCost / 25));
        }

        long start = System.currentTimeMillis();
        if (midCost == null || midCost < TimeUnit.SECONDS.toMillis(30) || lucky < 1) {
            boolean usedNormalQuery = true;
            resultStress = stressMapper.readStressData(mineDb);
            // cost, 如果是空，说明没有数据
            if (!resultStress.isEmpty()) {
                Stress maxTimeStress = resultStress.stream().max(Comparator.comparing(Stress::getCollectiontime)).get();
                midStressMinDate.put(mineDb, maxTimeStress.getCollectiontime());
            }
            long cost = (System.currentTimeMillis() - start);
            log.info("try use normal get stress data, cost => {}", cost);
            midStressReadCost.put(mineDb, cost);
        } else {
            List<Stress> allTimeStress = stressMapper.readJustByTime(DateUtil.formatDate(maxTime.getTime()), readStressCount);
            resultStress = allTimeStress.stream()
                    .filter(stress -> Objects.equals(stress.getMinecode(), mineDb))
                    .collect(Collectors.toList());
            // 如果不是空，筛选当前矿的数据,Oracle最多处理1000条数据
            if (!resultStress.isEmpty()) {
                if (resultStress.size() > 1000) {
                    resultStress = resultStress.subList(0, 1000);
                }
                Stress maxTimeStress = resultStress.stream().max(Comparator.comparing(Stress::getCollectiontime)).get();
                midStressMinDate.put(mineDb, maxTimeStress.getCollectiontime());
            } else {// 如果是空，用所有数据中最大的
                if (!allTimeStress.isEmpty()) {
                    Stress maxTimeStress = allTimeStress.stream().max(Comparator.comparing(Stress::getCollectiontime)).get();
                    midStressMinDate.put(mineDb, maxTimeStress.getCollectiontime());
                }
            }
            log.info("try use fast get stress data, cost => {}", (System.currentTimeMillis() - start));
        }

        return resultStress;
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
    public List<String> getMidAreaNameCache(String mineDb) {
        Object orDefault = midDataBaseCache.getOrDefault("AREANAME" + mineDb, null);
        return orDefault == null ? Collections.emptyList() :
                ((List<String>) orDefault).stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void restoreMineCache(String mineCode) {
        midDataBaseCache.remove("STRESS" + mineCode);
        midDataBaseCache.remove("QUAKE" + mineCode);
        midDataBaseCache.remove("AREANAME" + mineCode);
    }

}
