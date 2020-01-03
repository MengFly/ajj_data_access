package com.akxy.util;

import java.util.function.Function;

public class ParseUtil {

    public static <N> N getIValue(Number number, Function<String, N> parseFun) {
        String tmp = String.valueOf(number == null ? 0L : number.longValue());
        return parseFun.apply(tmp);
    }

    public static <N> N getFValue(Number number, Function<String, N> parseFun) {
        String tmp = String.valueOf(number == null ? 0. : number.doubleValue());
        return parseFun.apply(tmp);
    }

    public static <T> T getOrDefault(T t, T defaultValue) {
        return t == null ? defaultValue : defaultValue;
    }
}
