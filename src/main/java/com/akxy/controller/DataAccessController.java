package com.akxy.controller;

import com.akxy.service.IDataAccessService;
import com.akxy.util.CollectionUtil;
import com.akxy.util.TaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataAccessController {

    @Value("${spring.datasource.names}")
    public String primaryDB;

    @Autowired
    private IDataAccessService iDataAccessService;

    /**
     * 从中间库读取，计算Stress数据
     */
    public boolean readAndCalculate(List<String> childMines, long globalStep) {
        // 如果没有数据要进行分析
        boolean hasDataNeedAnalysis = !iDataAccessService.hasNeedAnalysisData();
        log.info(">>>>>>>>>>>>>>[Step {}]DataAccess Service Start<<<<<<<<<<<<<<<", globalStep);
        TaskUtil.getInstance().splitItemTaskExec(childMines, (customDB, integer) -> {

            Map<String, Object> map = iDataAccessService.getMapCache(primaryDB, customDB);
            Collection<?> stressData = (Collection<?>) map.get("STRESS" + customDB);
            Collection<?> quakeData = (Collection<?>) map.get("QUAKE" + customDB);
            if (CollectionUtils.isEmpty(stressData) && CollectionUtils.isEmpty(quakeData)) {
                log.info(">> [{}] 无数据", customDB);
            } else {
                log.info(">> [{}] 查询到 -> 应力({})条，微震({})条", customDB,
                        CollectionUtil.size(stressData), CollectionUtil.size(quakeData));
                iDataAccessService.copyDBToLocal(customDB);
                iDataAccessService.configArea(primaryDB, customDB);
                iDataAccessService.writeToMeasurePoint(primaryDB, customDB);
                iDataAccessService.readAndCalculateStress(primaryDB, customDB);
                iDataAccessService.readAndCalculateQuake(primaryDB, customDB);
                iDataAccessService.updatePointTime(primaryDB, customDB);
            }
            iDataAccessService.writeToPlatform(primaryDB, customDB);

        });
        log.info(">>>>>>>>>>>>>>[Step {}]DataAccess Service End<<<<<<<<<<<<<<<", globalStep);
        return hasDataNeedAnalysis;
    }

}
