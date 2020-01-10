package com.akxy.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangp
 */
public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    public static List<String> dataSourceNames = new ArrayList<>();

    public static void setDataSource(String dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }

    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 恢复数据源
     */
    public static void restoreDataSource() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 查看数据源是否存在
     */
    public static boolean containsDataSource(String dataSourceName) {
        return dataSourceNames.contains(dataSourceName);
    }
}
