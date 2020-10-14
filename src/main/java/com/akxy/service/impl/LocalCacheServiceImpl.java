package com.akxy.service.impl;

import com.akxy.DataAccessApplication;
import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.Quake;
import com.akxy.entity.Stress;
import com.akxy.mapper.MineMapper;
import com.akxy.mapper.QuakeMapper;
import com.akxy.mapper.StressMapper;
import com.akxy.service.ILocalCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wangp
 */
@SuppressWarnings("unchecked")
@Service
@Slf4j
public class LocalCacheServiceImpl implements ILocalCacheService {
    private static final int MAX_ROW_NUM = 10000;
    private static final int ONCE_NUM = 1000;
    private static final int MINE_HANDLE_NUM = 1000;

    private Map<String, Queue<Stress>> stressCache = new ConcurrentHashMap<>();

    private LoadDataThread loadDataRunnable = new LoadDataThread();


    @Override
    public void minusRowNum(int rowNum) {
        loadDataRunnable.minusRowNum(rowNum);
    }

    @Override
    public void startGetDataThread(List<String> mines) {
        for (String mine : mines) {
            stressCache.put(mine, new ConcurrentLinkedDeque<>());
        }
        DataAccessApplication.execute(loadDataRunnable);
    }

    private class LoadDataThread implements Runnable {
        private volatile int rowNum = 0;
        private Lock lock = new ReentrantLock();
        private Condition rowNumFull = lock.newCondition();

        synchronized void minusRowNum(int rowNum) {
            this.rowNum -= rowNum;
            log.info("rowNum is " + this.rowNum);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("加载数据线程");
            int step = 0;
            while (true) {
                try {
                    if (rowNum > MAX_ROW_NUM) {
                        Thread.sleep(10000);
                        log.info("queue full, sleep 10s");
                        continue;
                    }
                    step++;
                    long startTime = System.currentTimeMillis();
                    List<Stress> stresses = stressMapper.readByRowNumTimeAsc(rowNum + 1, rowNum + ONCE_NUM);
                    if (stresses.isEmpty()) {
                        log.info("无数据 sleep 10s");
                        Thread.sleep(10000);
                        continue;
                    }
                    rowNum += stresses.size();
                    log.info("读取应力数据 {} 条", stresses.size());
                    List<Stress> notContains = new ArrayList<>();
                    for (Stress stress : stresses) {
                        if (stressCache.containsKey(stress.getMinecode())) {
                            stressCache.get(stress.getMinecode()).add(stress);
                        } else {
                            notContains.add(stress);
                        }
                    }
                    if (!notContains.isEmpty()) {
                        int i = stressMapper.deleteGroupData(notContains);
                        log.info("不存在的矿区数量{}条, 删除{}条", notContains.size(), i);
                        minusRowNum(notContains.size());
                    }
                    log.info("[Step {}]读取数据完成,耗时 {} mms, NowRowNum={}",
                            step, System.currentTimeMillis() - startTime, rowNum);

                } catch (Exception e) {
                    log.error("LoadDataError:", e);
                }
            }

        }
    }


    @Override
    public List<Stress> getMineStress(String mineCode) {
        Queue<Stress> stresses = stressCache.get(mineCode);
        int readCount = 0;
        List<Stress> read = new ArrayList<>();
        while (readCount < MINE_HANDLE_NUM && !stresses.isEmpty()) {
            Stress poll = stresses.poll();
            read.add(poll);
            readCount++;
        }
        return read;
    }

    /**
     * 中间库缓存，每一次读取都需要重新读取，因此放在同一个列表里面，方便清理
     */
    private Map<String, Object> midDataBaseCache = new ConcurrentHashMap<>();

    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;
    @Autowired
    private MineMapper mineMapper;


    @Override
    public void prepareMidCache(String primaryDb, String mineDb) {
        // 中间库数据库
        DynamicDataSourceContextHolder.setDataSource(primaryDb);
        // 1000 条微震信息
        List<Quake> quakeList = quakeMapper.readQuakeData(mineDb);
        midDataBaseCache.put("QUAKE" + mineDb, quakeList);
        DynamicDataSourceContextHolder.restoreDataSource();
    }

    @Override
    public List<Quake> getMidQuakeCache(String mineDb) {
        Object orDefault = midDataBaseCache.getOrDefault("QUAKE" + mineDb, null);
        return orDefault == null ? Collections.emptyList() : (List<Quake>) orDefault;
    }


    @Override
    public void restoreMineCache(String mineCode) {
        midDataBaseCache.remove("QUAKE" + mineCode);
    }

}
