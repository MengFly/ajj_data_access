package com.akxy.util;

import org.springframework.util.ObjectUtils;

import java.util.regex.Matcher;

/**
 * @author wangp
 */
public class StringUtil {

    private StringUtil() {
    }

    public static String join(String separator, Object... res) {
        if (res == null || res.length == 0) {
            return "";
        }
        if (res.length == 1 && ObjectUtils.isArray(res)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object re : res) {
            sb.append(re).append(separator);
        }
        return sb.substring(0, sb.length() - separator.length());
    }


    public static String format(String marker, Object... params) {
        if (params == null || params.length == 0) {
            return marker;
        }
        String formatResult = marker;
        for (Object param : params) {
            formatResult = marker.replaceFirst("\\{}", Matcher.quoteReplacement(param.toString()));
        }
        return formatResult;
    }

    public static String subSafely(String res, int start, int end) {
        start = Math.min(start, res.length());
        end = Math.min(end, res.length());
        return res.substring(start, end);
    }



}
