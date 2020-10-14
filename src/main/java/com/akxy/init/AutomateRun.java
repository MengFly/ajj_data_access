package com.akxy.init;

import com.akxy.DataAccessApplication;
import com.akxy.entity.Quake;
import com.akxy.entity.Stress;
import com.akxy.mapper.MineMapper;
import com.akxy.mapper.QuakeMapper;
import com.akxy.mapper.StressMapper;
import com.akxy.service.IDataAccessService;
import com.akxy.service.ILocalCacheService;
import com.akxy.util.ParseUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    static final long TIME_INTERVAL_SECOND = 60;

    @Value("${custom.datasource.names}")
    public String customDbs;

    @Value("${spring.datasource.names}")
    public String primaryDb;

    @Value("${check.lock:true}")
    public boolean checkLock;

    @Value("${check.time:15}")
    public long checkTime;

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
        localCacheService.startGetDataThread(childMines);
        for (String childMine : childMines) {
            String mineName = ParseUtil.getOrDefault(mineMapper.findNameByCode(childMine), "UnKnown");
            DataAccessApplication.execute(getCalculateRunnable(childMine, mineName));
        }
    }

    private Runnable getCalculateRunnable(String mineCode, String mineName) {
        return () -> {
            int step = 0;
            Thread.currentThread().setName(mineName);
            while (true) {
                try {
                    log.info(">>>>>>>>>>>>>>[{}] [Step {}] DataAccess Service Start", mineCode, step);
                    lastStepTime.put(mineCode, System.currentTimeMillis());
                    readAndCalculate(mineCode);
                } catch (Exception e) {
                    log.error("分析出错", e);
                    sleep(TimeUnit.SECONDS, TIME_INTERVAL_SECOND);
                } finally {
                    log.info(">>>>>>>>>>>>>>[{}] [Step {}] DataAccess Service End {} mms", mineCode, step++,
                            (System.currentTimeMillis() - lastStepTime.getOrDefault(mineCode, 0L)));
                }
            }
        };
    }

    /**
     * 从中间库读取，计算Stress数据
     */
    public void readAndCalculate(String mineCode) {
        localCacheService.prepareMidCache(primaryDb, mineCode);
        List<Stress> stresses = localCacheService.getMineStress(mineCode);
        List<Quake> quakes = localCacheService.getMidQuakeCache(mineCode);
        int selectCount = quakes.size() + stresses.size();
        try {
            if (stresses.isEmpty() && quakes.isEmpty()) {
                log.info(">> 无数据");
                sleep(TimeUnit.SECONDS, TIME_INTERVAL_SECOND);
            } else {
                log.info(">> 查询到 -> 应力({})条，微震({})条", stresses.size(), quakes.size());
                Set<String> areaNames = getAreaNames(quakes, stresses);
                iDataAccessService.configArea(primaryDb, mineCode, areaNames);
                iDataAccessService.writeNotExistsMeasurePoint(mineCode, stresses);
                iDataAccessService.readAndCalculateStress(primaryDb, mineCode, stresses);
                iDataAccessService.readAndCalculateQuake(primaryDb, mineCode, quakes);
            }
        } finally {
            iDataAccessService.writeToPlatform(primaryDb, mineCode);
//             中间库的缓存信息在所有矿区都分析结束的时候就没有用了，删除掉即可
            localCacheService.restoreMineCache(mineCode);
            if (selectCount < 500) {
                sleep(TimeUnit.SECONDS, 10);
            }
        }
    }

    private Set<String> getAreaNames(List<Quake> midQuakeCache, List<Stress> midStressCache) {
        Set<String> areaNames = midQuakeCache.stream().parallel().map(Quake::getAreaname).collect(Collectors.toSet());
        areaNames.addAll(midStressCache.stream().parallel().map(Stress::getAreaname).collect(Collectors.toSet()));
        return areaNames;
    }

    /**
     * 检测程序是否卡死
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkIsBlock() {
        if (checkLock) {
            Long aLong = lastStepTime.values().stream().min(Long::compareTo).orElse(System.currentTimeMillis());
            if (System.currentTimeMillis() - aLong > TimeUnit.MINUTES.toMillis(checkTime)) {
                log.info("[CheckBlock] 程序卡死");
                System.exit(-1);
            } else {
                log.info("[CheckBlock] 程序正正常运行");
            }
        } else {
            log.info("不检测卡死");
        }

    }


    @SneakyThrows
    public void sleep(TimeUnit timeUnit, long num) {
        log.info("Sleep {} {}", num, timeUnit.toString());
        timeUnit.sleep(num);
    }
}
