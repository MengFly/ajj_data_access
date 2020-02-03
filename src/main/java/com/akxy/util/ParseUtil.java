package com.akxy.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

public class ParseUtil {

    private static final DateFormat DATE_FORMAT =
            SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);

    /**
     * 获取整数类型数据
     */
    public static <N> N getIValue(Number number, Function<String, N> parseFun) {
        String tmp = String.valueOf(number == null ? 0L : number.longValue());
        return parseFun.apply(tmp);
    }

    /**
     * 获取浮点数类型的数据
     */
    public static <N> N getFValue(Number number, Function<String, N> parseFun) {
        String tmp = String.valueOf(number == null ? 0. : number.doubleValue());
        return parseFun.apply(tmp);
    }

    public static <T> T getOrDefault(T t, T defaultValue) {
        return t == null ? defaultValue : defaultValue;
    }

    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }

}
