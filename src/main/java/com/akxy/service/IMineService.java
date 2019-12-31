package com.akxy.service;

public interface IMineService {
	
	/**
	 * 根据MineCode获取分数据库名称
	 * @param mineCode 分数据库编码
	 * @return 分数据库名字
	 */
	public String getMineNameByCode(String mineCode);

}
