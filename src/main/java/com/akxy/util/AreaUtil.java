package com.akxy.util;

import com.akxy.mapper.AreaMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AreaUtil implements Closeable {

    /**
     * 获取全矿id
     *
     * @return 全矿id
     */
    public static Long getAllMineId(String mineDb) {
        return Long.valueOf(mineDb.substring(mineDb.lastIndexOf("0") + 1) + "000");
    }


    /**
     * 以下对象为从数据库中查找areaId做辅助工作，因为1000条的数据中肯定工作面会重复，把查找过得数据存起来防止过量查询数据库
     */
    private Map<String, Long> nameIdCache;
    private AreaMapper areaMapper;

    public AreaUtil(AreaMapper areaMapper) {
        this.areaMapper = areaMapper;
        this.nameIdCache = new HashMap<>(16);
    }

    public Long getId(String name, String mineDb) {
        Long areaId = nameIdCache.getOrDefault(name, null);
        if (areaId == null) {
            areaId = areaMapper.findIdByName(name);
            if (areaId != null) {
                nameIdCache.put(name, areaId);
            }
        }
        if (areaId == null) {
            return getAllMineId(mineDb);
        }
        return areaId;
    }

    public void destroy() {

    }

    @Override
    public void close() {
        log.info("关闭");
        this.areaMapper = null;
        this.nameIdCache.clear();
        this.nameIdCache = null;
    }
}
