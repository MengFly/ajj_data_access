package com.akxy.init;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.controller.DataAccessController;
import com.akxy.mapper.QuakeMapper;
import com.akxy.mapper.StressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author wangp
 */
@Component
@Slf4j
public class AutomateRun implements ApplicationRunner {
    private long globalStep = 0;

    private long lastStepTime = System.currentTimeMillis();

    /**
     * 检测阻塞的间隔时间，超过十五分钟时间没有更新说明程序被阻塞住了
     */
    private static final long BLOCK_TIME = 15 * 1000 * 60L;


    static final long TIME_INTERVAL_SECOND = 30;

    @Value("${custom.datasource.names}")
    public String customDbs;

    @Autowired
    private DataAccessController dataAccessController;
    @Autowired
    private StressMapper stressMapper;
    @Autowired
    private QuakeMapper quakeMapper;

    @Override
    public void run(ApplicationArguments args) {
        // 需要分析的子矿区数据库列表
        List<String> childMines = Arrays.stream(customDbs.split(",")).filter(
                s -> !"copy".equals(s) && !"1000".equals(s)
        ).collect(Collectors.toList());
        new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "main"))
                .scheduleWithFixedDelay(() -> {
                    lastStepTime = System.currentTimeMillis();
                    boolean isHandleData = dataAccessController.readAndCalculate(childMines, globalStep++);
                    if (!isHandleData) {
                        log.info("矿区没有数据需要分析 SLEEP {} S", TIME_INTERVAL_SECOND);
                        try {
                            TimeUnit.SECONDS.sleep(TIME_INTERVAL_SECOND);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    log.info("[Step {}] 处理结束，耗时 {}mms", globalStep, (System.currentTimeMillis() - lastStepTime));
                }, 0, TIME_INTERVAL_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void deleteLastMonthData() {
        // 如果程序正常运行的话，3天内应该能处理的数据
        int shouldBeAnalysisDataCount = 24 * 60 / 5 * 1000 * 3;
        log.info(">>>> [Scheduled] 开始检查中间库一个月之前的数据进行删除");
        DynamicDataSourceContextHolder.setDataSource("ds0");
        Integer stressCount = stressMapper.stressCount();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, -4);
        Timestamp time = new Timestamp(instance.getTimeInMillis());
        if (stressCount != null && stressCount > shouldBeAnalysisDataCount / 2) {
            log.info(">>> [Scheduled] 应力数据超过可处理上限，进行删除");
            int deleteStressCount = stressMapper.deleteByTimeLessThan(time);
            log.info(">>> [Scheduled] 删除{}之前的应力数({})条",
                    SimpleDateFormat.getDateInstance().format(instance.getTime()), deleteStressCount);

        }
        Integer quakeCount = quakeMapper.quakeCount();
        if (quakeCount != null && quakeCount > shouldBeAnalysisDataCount / 2) {
            log.info(">>> [Scheduled] 微震数据({})超过可处理上限({})，进行删除", quakeCount, shouldBeAnalysisDataCount / 2);
            int deleteQuakeCount = quakeMapper.deleteByTimeLessThan(time);
            log.info(">>> [Scheduled] 删除{}之前的微震数({})条",
                    SimpleDateFormat.getDateInstance().format(instance.getTime()), deleteQuakeCount);

        }
        log.info(">>>> [Scheduled] 定时任务执行结束");
    }

    /**
     * 检测程序是否卡死
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkIsBlock() {
        if (System.currentTimeMillis() - lastStepTime > BLOCK_TIME) {
            log.info("[CheckBlock] 程序卡死");
            System.exit(-1);
        } else {
            log.info("[CheckBlock] 程序正正常运行");
        }

    }
}
