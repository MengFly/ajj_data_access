package com.akxy.util;
 
 
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Date;

import com.akxy.entity.AreaDataInfo;
import com.akxy.entity.AreaTopDataInfo;
 
/**
 * Created by wangzhipeng on 2017/3/16.
 */
public class CopyUtils {
    public static void Copy(Object source, Object dest) throws Exception {
        // 获取属性
        BeanInfo sourceBean = Introspector.getBeanInfo(source.getClass(),Object.class);
        PropertyDescriptor[] sourceProperty = sourceBean.getPropertyDescriptors();
 
        BeanInfo destBean = Introspector.getBeanInfo(dest.getClass(),Object.class);
        PropertyDescriptor[] destProperty = destBean.getPropertyDescriptors();
 
        try {
            for (PropertyDescriptor propertyDescriptor : sourceProperty) {
                for (PropertyDescriptor descriptor : destProperty) {
                    if (propertyDescriptor.getName().equals(descriptor.getName()) && propertyDescriptor.getPropertyType() == descriptor.getPropertyType()) {
                        // 调用source的getter方法和dest的setter方法
                        descriptor.getWriteMethod().invoke(dest, propertyDescriptor.getReadMethod().invoke(source));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("属性复制失败:" + e.getMessage());
        }
    }
    public static void main(String[] args) throws Exception{
        AreaDataInfo areaDataInfo=new AreaDataInfo();
        areaDataInfo.setAcquisitionTime(new Date());
        areaDataInfo.setAreaId(1001L);
        areaDataInfo.setAreaLevel("绿色");
        areaDataInfo.setAreaValue((short)2);
        areaDataInfo.setMemo("测试");
        areaDataInfo.setQuakeValue((short)110);
        areaDataInfo.setStressValue((short)3);
        AreaTopDataInfo areaTopDataInfo=new AreaTopDataInfo();
        CopyUtils.Copy(areaDataInfo,areaTopDataInfo);
        System.out.println(areaTopDataInfo.toString());
    }
}