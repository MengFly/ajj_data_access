package com.akxy.util;

public class AreaUtil {

    /**
     * 获取全矿id
     * @return 全矿id
     */
    public static Long getAllMineId(String mineDb) {
        return Long.valueOf(mineDb.substring(mineDb.lastIndexOf("0") + 1) + "000");
    }
}
