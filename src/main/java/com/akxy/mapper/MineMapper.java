package com.akxy.mapper;

import com.akxy.entity.Mine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MineMapper {

    /**
     * 遍历数据库配置表
     */
    List<Mine> listMines(String customDB);

}