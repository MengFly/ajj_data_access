package com.akxy.init;

import com.akxy.controller.DataAccessController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AutomateRun implements ApplicationRunner {
    private long globalStep = 0;
    static final long TIME_INTERVAL = 60000;

    @Value("${custom.datasource.names}")
    public String customDBS;

    @Autowired
    private DataAccessController dataAccessController;

    @Override
    public void run(ApplicationArguments args) {
        // 需要分析的子矿区数据库列表
        List<String> childMines = Arrays.stream(customDBS.split(",")).filter(
                s -> !s.equals("copy") && !s.equals("1000")
        ).collect(Collectors.toList());

        while (true) {
            long startTime = System.currentTimeMillis();

            boolean isHandleData = dataAccessController.readAndCalculate(childMines, globalStep++);
            if (!isHandleData) {
                log.info("矿区没有数据需要分析 SLEEP {} MMS", TIME_INTERVAL);
                try {
                    TimeUnit.MILLISECONDS.sleep(TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("[Step {}] 处理结束，耗时 {}mms", globalStep, (System.currentTimeMillis() - startTime));
        }
    }

    // 每天凌晨一点执行一次
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void deleteLastMonthData() {
//        log.info("检查中间库一个月之前的数据进行删除");
//        DynamicDataSourceContextHolder.setDataSource("ds0");
//        Calendar instance = Calendar.getInstance();
//        instance.add(Calendar.MONTH, -1);
//        Timestamp time = new Timestamp(instance.getTimeInMillis());
//        int deleteStressCount = stressMapper.deleteByTimeLessThan(time);
//        int deleteQuakeCount = quakeMapper.deleteByTimeLessThan(time);
//        log.info("删除结束,删除{}之前 应力{}条数据， 微震{}条数据",
//                SimpleDateFormat.getDateTimeInstance().format(instance.getTime()), deleteStressCount, deleteQuakeCount);
//    }

}
