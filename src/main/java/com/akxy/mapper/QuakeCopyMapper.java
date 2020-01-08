package com.akxy.mapper;

import com.akxy.entity.Quake;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuakeCopyMapper {

    /**
     * copy Quake表到本地
     */
    void copyQuake(List<Quake> quakes);
}