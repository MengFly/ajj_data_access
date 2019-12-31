package com.akxy.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.akxy.entity.PosResult;

@Mapper
public interface PosResultMapper {

	Date findNewDate();

	/**
	 * 更新POSResult
	 * 
	 * @param posResult
	 */
	public void updatePos(PosResult posResult);

	/**
	 * 最新数据上传时间
	 * 
	 * @return
	 */
	public Date greatestNewTime();

	/**
	 * 将组装好的数据写入到POSRESULT表中
	 * 
	 * @param quake
	 */
	public void writeToPost(PosResult posResult);

	/**
	 * 依据时间判断POSRESULT表中是否含有该数据
	 * 
	 * @param collectionTime
	 * @return
	 */
	public Integer judgeContain(@Param("collectionTime") Date collectionTime);

	/**
	 * 依据时间删除POSRESULT表中含有的数据
	 * 
	 * @param collectionTime
	 * @return
	 */
	public void deletePOSData(@Param("collectionTime") Date collectionTime);

	/**
	 * 批量更新POSRESULT
	 * 
	 * @param posResults
	 */
	public void updateGroupData(List<PosResult> posResults);

	/**
	 * 批量插入POSRESULT
	 * 
	 * @param posResults
	 */
	public void insertGroupData(List<PosResult> posResults);

	/**
	 * 遍历POSRESULT表
	 * 
	 * @return
	 */
	public List<PosResult> getAllPos();

}