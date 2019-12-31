package com.akxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.akxy.entity.Stress;
import com.akxy.entity.StressCopy;

@Mapper
public interface StressCopyMapper {
	
	/**
	 * copy Stress
	 * @param stresses
	 */
	void copyStress(List<Stress> stresses);
	
    int deleteByPrimaryKey(Integer id);

    int insert(StressCopy record);

    int insertSelective(StressCopy record);

    StressCopy selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(StressCopy record);

    int updateByPrimaryKey(StressCopy record);
}