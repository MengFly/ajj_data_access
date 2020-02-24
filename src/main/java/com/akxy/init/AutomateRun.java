package com.akxy.init;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.entity.Quake;
import com.akxy.entity.Stress;
import com.akxy.mapper.MineMapper;
import com.akxy.mapper.QuakeMapper;
import com.akxy.mapper.StressMapper;
import com.akxy.service.IDataAccessService;
import com.akxy.service.ILocalCacheService;
import com.akxy.util.ParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wangp
 */
@Component
@Slf4j
public class AutomateRun implements ApplicationRunner {

    /**
     * 使用map存储每一个矿区的上次分析时间
     */
    private Map<String, Long> lastStepTime = new HashMap<>();

    /**
     * 检测阻塞的间隔时间，超过十五分钟时间没有更新说明程序被阻塞住了
     */
    private static final long BLOCK_TIME = 15 * 1000 * 60L;


    static final long TIME_INTERVAL_SECOND = 30;

    @Value("${custom.datasource.names}")
    public String customDbs;

    @Value("${spring.datasource.names}")
    public String primaryDb;

    @Autowired
    private IDataAccessService iDataAccessService;

    @Autowired
    private ILocalCacheService localCacheService;
    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;
    @Autowired
    private MineMapper mineMapper;

    @Override
    public void run(ApplicationArguments args) {
        // 需要分析的子矿区数据库列表
        List<String> childMines = Arrays.stream(customDbs.split(",")).filter(
                s -> !"copy".equals(s) && !"1000".equals(s)
        ).collect(Collectors.toList());
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (String childMine : childMines) {
            String mineName = ParseUtil.getOrDefault(mineMapper.findNameByCode(childMine), "UnKnown");
            executorService.execute(getCalculateRunnable(childMine, mineName));
        }
    }

    private Runnable getCalculateRunnable(String mineCode, String mineName) {
        return () -> {
            int step = 0;
            Thread.currentThread().setName(mineName);
            while (true) {
                try {
                    log.info(">>>>>>>>>>>>>> [Step {}] DataAccess Service Start<<<<<<<<<<<<<<<", step);
                    lastStepTime.put(mineCode, System.currentTimeMillis());
                    readAndCalculate(mineCode);
                    TimeUnit.SECONDS.sleep(TIME_INTERVAL_SECOND);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    log.info(">>>>>>>>>>>>>> [Step {}] DataAccess Service End {}mms<<<<<<<<<<<<<<<", step++,
                            (System.currentTimeMillis() - lastStepTime.getOrDefault(mineCode, 0L)));
                }
            }
        };
    }

    /**
     * 从中间库读取，计算Stress数据
     */
    public void readAndCalculate(String mineCode) throws Exception {
        localCacheService.prepareMidCache(primaryDb, mineCode);
        List<Stress> midStressCache = localCacheService.getMidStressCache(mineCode);
        List<Quake> midQuakeCache = localCacheService.getMidQuakeCache(mineCode);
        try {
            if (midStressCache.isEmpty() && midQuakeCache.isEmpty()) {
                log.info(">> [{}] 无数据", mineCode);
                TimeUnit.SECONDS.sleep(TIME_INTERVAL_SECOND);
            } else {

                log.info(">> [{}] 查询到 -> 应力({})条，微震({})条",
                        mineCode, midStressCache.size(), midQuakeCache.size());
                iDataAccessService.configArea(primaryDb, mineCode);
                iDataAccessService.writeNotExistsMeasurePoint(mineCode);
                iDataAccessService.readAndCalculateStress(primaryDb, mineCode);
                iDataAccessService.readAndCalculateQuake(primaryDb, mineCode);
            }
        } finally {
            iDataAccessService.writeToPlatform(primaryDb, mineCode);
        }
        // 中间库的缓存信息在所有矿区都分析结束的时候就没有用了，删除掉即可
        localCacheService.restoreMineCache(mineCode);
    }

    /**
     * 检测程序是否卡死
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkIsBlock() {
        Long aLong = lastStepTime.values().stream().min(Long::compareTo).orElse(System.currentTimeMillis());
        if (System.currentTimeMillis() - aLong > BLOCK_TIME) {
            log.info("[CheckBlock] 程序卡死");
            System.exit(-1);
        } else {
            log.info("[CheckBlock] 程序正正常运行");
        }

    }
}
