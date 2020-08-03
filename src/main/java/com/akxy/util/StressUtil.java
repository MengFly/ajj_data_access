package com.akxy.util;

import com.akxy.entity.Stress;
import com.akxy.entity.StressDataInfo;

public class StressUtil {

    /**
     * 判断应力数据是否应该被保存
     *
     * @param stress 应力数据
     * @return 是否应该被保存
     */
    public static boolean needSave(Stress stress) {
        return stress.getValue() <= 30 && stress.getCollectiontime() != null;
    }

    /**
     * 判断应力数据是否应该被保存
     * @param dataInfo 应力数据
     * @return  是否应该被保存
     */
    public static boolean needSave(StressDataInfo dataInfo) {
        return dataInfo.getPValue() <= 30 && dataInfo.getAcquisitionTime() != null;
    }

}
