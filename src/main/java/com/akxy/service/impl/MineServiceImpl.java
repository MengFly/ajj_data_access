package com.akxy.service.impl;

import com.akxy.configuration.DynamicDataSourceContextHolder;
import com.akxy.mapper.MineMapper;
import com.akxy.service.IMineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MineServiceImpl implements IMineService {
	
	@Autowired
	private MineMapper mineMapper;

	@Override
	public String getMineNameByCode(String mineCode) {
		DynamicDataSourceContextHolder.setDataSource("ds0");
		return mineMapper.getMineNameByCode(mineCode);
	}

}
