package com.akxy.common;

import com.akxy.util.DateUtil;
import com.akxy.util.StringUtil;
import lombok.ToString;
import org.slf4j.Logger;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author wangp
 */
public class LogFilter {
    private static final int SIGN_OUT_LIMIT = 10;
    private static ConcurrentMap<String, LogRecordItem> RECORD_ITEM_MAP = new ConcurrentHashMap<>(16);

    private LogFilter() {
    }


    public static void error(Logger log, Exception e, String marker, Object... params) {
        String format = StringUtil.format(marker, params);

        String name = e.getClass().getName();
        int hash = e.getMessage().hashCode();
        LogRecordItem item = RECORD_ITEM_MAP.getOrDefault(name + hash, null);
        if (item != null) {
            updateItem(log, item, e, format);
        } else {
            recordItem(log, format, e);
        }
    }

    private static void updateItem(Logger log, LogRecordItem item, Exception e, String format) {
        item.causeCount++;
        // 每过10秒清除一次标识
        item.signCount -= ((item.lastCauseTime - System.currentTimeMillis()) / (1000 * 60 * 10));
        item.lastCauseTime = System.currentTimeMillis();
        // 如果不到标志位，或者日志是前一天的日志，照样输出日志
        if (item.signCount <= SIGN_OUT_LIMIT || item.lastOutTime < DateUtil.dayStart().getTime()) {
            log.error(format, e);
            item.lastOutTime = System.currentTimeMillis();
        } else {
            String errorMessage = StringUtil.subSafely(e.getMessage(), 0, 50);
            String tips = StringUtil.format("。错误重复，检查日志{}=>{}, Description:{}...",
                    new Date(item.lastOutTime), item.exceptionName, errorMessage);
            log.error(format + tips);
        }
        item.signCount++;
    }


    private static void recordItem(Logger log, String format, Exception e) {
        LogRecordItem item = new LogRecordItem();
        item.causeCount = 1;
        item.exceptionName = e.getClass().getName();
        item.hash = e.getMessage().hashCode();
        item.lastOutTime = System.currentTimeMillis();
        item.lastCauseTime = System.currentTimeMillis();
        item.signCount = 1;
        RECORD_ITEM_MAP.put(item.exceptionName + item.hash, item);
        log.error(format, e);
    }


    @ToString
    static class LogRecordItem {
        String exceptionName;
        int hash;
        int causeCount;
        long lastOutTime;
        long lastCauseTime;
        int signCount;
    }

}
