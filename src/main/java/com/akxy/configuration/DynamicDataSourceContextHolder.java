package com.akxy.configuration;

import java.util.ArrayList;
import java.util.List;

public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
    public static List<String> dataSourceNames = new ArrayList<String>();

    public static void setDataSource(String dataSource) {
        contextHolder.set(dataSource);
    }

    public static String getDataSource() {
        return contextHolder.get();
    }

    /**
     * 恢复数据源
     */
    public static void restoreDataSource() {
        contextHolder.remove();
    }

    /**
     * 查看数据源是否存在
     */
    public static boolean containsDataSource(String dataSourceName) {
        return dataSourceNames.contains(dataSourceName);
    }
}
