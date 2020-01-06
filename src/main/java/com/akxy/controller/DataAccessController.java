package com.akxy.controller;

import com.akxy.entity.Quake;
import com.akxy.entity.Stress;
import com.akxy.service.IDataAccessService;
import com.akxy.service.ILocalCacheService;
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

    @Autowired
    private ILocalCacheService localCacheService;

    /**
     * 从中间库读取，计算Stress数据
     */
    public boolean readAndCalculate(List<String> childMines, long globalStep) {
        // 如果没有数据要进行分析
        boolean hasDataNeedAnalysis = iDataAccessService.hasNeedAnalysisData();
        log.info(">>>>>>>>>>>>>> [Step {}] DataAccess Service Start<<<<<<<<<<<<<<<", globalStep);
        TaskUtil.getInstance().splitItemTaskExec(childMines, (customDB, integer) -> {
            localCacheService.prepareMidCache(primaryDB, customDB);
            String mineName = localCacheService.getMineName(customDB);
            List<Stress> midStressCache = localCacheService.getMidStressCache(customDB);
            List<Quake> midQuakeCache = localCacheService.getMidQuakeCache(customDB);
            if (midStressCache.isEmpty() && midQuakeCache.isEmpty()) {
                log.info(">> [{}-{}] 无数据", customDB, mineName);
            } else {
                log.info(">> [{}-{}] 查询到 -> 应力({})条，微震({})条", customDB, mineName,
                        midStressCache.size(), midQuakeCache.size());
                iDataAccessService.copyDBToLocal(customDB, mineName);
                iDataAccessService.configArea(primaryDB, customDB, mineName);
                iDataAccessService.writeNotExistsMeasurePoint(customDB, mineName);
                iDataAccessService.readAndCalculateStress(primaryDB, customDB, mineName);
                iDataAccessService.readAndCalculateQuake(primaryDB, customDB, mineName);
                iDataAccessService.updatePointTime(primaryDB, customDB, mineName);
            }
            iDataAccessService.writeToPlatform(primaryDB, customDB, mineName);

        });
        // 中间库的缓存信息在所有矿区都分析结束的时候就没有用了，删除掉即可
        localCacheService.restoreAllMidCache();
        log.info(">>>>>>>>>>>>>> [Step {}] DataAccess Service End<<<<<<<<<<<<<<<", globalStep);
        return hasDataNeedAnalysis;
    }

}
