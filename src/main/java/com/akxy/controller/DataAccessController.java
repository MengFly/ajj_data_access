package com.akxy.controller;

import com.akxy.entity.Quake;
import com.akxy.entity.Stress;
import com.akxy.service.IDataAccessService;
import com.akxy.service.ILocalCacheService;
import com.akxy.util.TaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangp
 */
@Service
@Slf4j
public class DataAccessController {

    @Value("${spring.datasource.names}")
    public String primaryDb;

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
        TaskUtil.getInstance().splitItemTaskExec(childMines, (customDb, integer) -> {
            localCacheService.prepareMidCache(primaryDb, customDb);
            String mineName = localCacheService.getMineName(customDb);
            List<Stress> midStressCache = localCacheService.getMidStressCache(customDb);
            List<Quake> midQuakeCache = localCacheService.getMidQuakeCache(customDb);
            if (midStressCache.isEmpty() && midQuakeCache.isEmpty()) {
                log.info(">> [{}-{}] 无数据", customDb, mineName);
            } else {
                log.info(">> [{}-{}] 查询到 -> 应力({})条，微震({})条", customDb, mineName,
                        midStressCache.size(), midQuakeCache.size());
                iDataAccessService.configArea(primaryDb, customDb, mineName);
                iDataAccessService.writeNotExistsMeasurePoint(customDb, mineName);
                iDataAccessService.readAndCalculateStress(primaryDb, customDb, mineName);
                iDataAccessService.readAndCalculateQuake(primaryDb, customDb, mineName);
                iDataAccessService.updatePointTime(primaryDb, customDb, mineName);
            }
            iDataAccessService.writeToPlatform(primaryDb, customDb, mineName);

        });
        // 中间库的缓存信息在所有矿区都分析结束的时候就没有用了，删除掉即可
        localCacheService.restoreAllMidCache();
        log.info(">>>>>>>>>>>>>> [Step {}] DataAccess Service End<<<<<<<<<<<<<<<", globalStep);
        return hasDataNeedAnalysis;
    }

}
