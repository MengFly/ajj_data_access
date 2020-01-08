package com.akxy.mapper;

import com.akxy.entity.Stress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StressCopyMapper {

    /**
     * copy Stress
     */
    void copyStress(List<Stress> stresses);
}