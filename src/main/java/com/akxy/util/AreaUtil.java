package com.akxy.util;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.mapper.AreaMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AreaUtil implements Closeable {

    /**
     * 以下对象为从数据库中查找areaId做辅助工作，因为1000条的数据中肯定工作面会重复，把查找过得数据存起来防止过量查询数据库
     */
    private Map<String, Long> nameIdCache;

    public AreaUtil() {
        this.nameIdCache = new ConcurrentHashMap<>(16);
    }

    public synchronized Long getId(String name, String customDb, AreaMapper areaMapper) {
        Long areaId = nameIdCache.getOrDefault(name, null);
        if (areaId == null) {
            DynamicDataSourceContextHolder.setDataSource(customDb);
            areaId = areaMapper.findIdByName(name);
            if (areaId != null) {
                nameIdCache.put(name, areaId);
            }
        }
        if (areaId == null) {
            return Long.valueOf(customDb.substring(customDb.lastIndexOf("0") + 1) + "000");
        }
        return areaId;
    }

    @Override
    public void close() {
        this.nameIdCache.clear();
        this.nameIdCache = null;
    }
}
