package com.akxy.common;

import java.io.Serializable;
import java.util.Date;

public class CompareUtil implements Serializable {

	public Date timeDate;
	public Long areaId;
	public Long mpId;
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CompareUtil) {
			
		}
		return super.equals(obj);
	}
	
}
