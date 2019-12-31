package com.akxy.entity;

import java.util.Date;

public class HiMineInfo extends HiMineInfoKey {
    private Date acquisitionTime;

    private Double stressValue;

    private Double quakeValue;

    private String memo;

    public Date getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(Date acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public Double getStressValue() {
        return stressValue;
    }

    public void setStressValue(Double stressValue) {
        this.stressValue = stressValue;
    }

    public Double getQuakeValue() {
        return quakeValue;
    }

    public void setQuakeValue(Double quakeValue) {
        this.quakeValue = quakeValue;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }
    private Long areaId;

    private Short type;
    
    private Long mpId;
    

    public Long getMpId() {
		return mpId;
	}

	public void setMpId(Long mpId) {
		this.mpId = mpId;
	}

	public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }
	@Override
	public String toString() {
		return "HiMineInfo [acquisitionTime=" + acquisitionTime + ", stressValue=" + stressValue + ", quakeValue="
				+ quakeValue + ", memo=" + memo + ", areaId=" + areaId + ", type=" + type + ", mpId=" + mpId + "]";
	}
}