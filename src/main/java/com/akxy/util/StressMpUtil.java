package com.akxy.util;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.mapper.StressMeasurePointMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StressMpUtil implements Closeable {

    /**
     * 以下对象为从数据库中查找areaId做辅助工作，因为1000条的数据中肯定工作面会重复，把查找过得数据存起来防止过量查询数据库
     */
    private Map<String, Long> nameIdCache;


    public StressMpUtil() {
        this.nameIdCache = new ConcurrentHashMap<>(16);
    }

    public synchronized Long getId(Long areaId, String tunnelName,
                                   Double distance, Double depth, String mineDb,
                                   StressMeasurePointMapper stressMeasurePointMapper) {
        tunnelName = ParseUtil.getOrDefault(tunnelName, "巷道");
        String sign = areaId + tunnelName + distance + depth;
        Long mpId = nameIdCache.getOrDefault(sign, null);
        if (mpId == null) {
            DynamicDataSourceContextHolder.setDataSource(mineDb);
            mpId = stressMeasurePointMapper.findIdBy(areaId, tunnelName, depth, distance);
            if (mpId != null) {
                nameIdCache.put(sign, mpId);
            }
        }
        if (mpId == null) {
            return 0L;
        }
        return mpId;
    }

    @Override
    public void close() {
        this.nameIdCache.clear();
        this.nameIdCache = null;
    }
}
