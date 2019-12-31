package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.akxy.entity.Quake;
import com.akxy.entity.QuakeCopy;

@Mapper
public interface QuakeCopyMapper {
	
	/**
	 * copy Quake表到本地
	 * @param quakes
	 */
	void copyQuake(List<Quake> quakes);
	
    int deleteByPrimaryKey(Integer id);

    int insert(QuakeCopy record);

    int insertSelective(QuakeCopy record);

    QuakeCopy selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QuakeCopy record);

    int updateByPrimaryKey(QuakeCopy record);
}