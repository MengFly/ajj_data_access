package com.akxy.entity;

import java.util.Date;

public class AreaDataInfo {
    private Long areaId;

    private Date acquisitionTime;

    private Short areaValue;

    private String areaLevel;

    private Short stressValue;

    private Short quakeValue;

    private String memo;

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Date getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(Date acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public Short getAreaValue() {
        return areaValue;
    }

    public void setAreaValue(Short areaValue) {
        this.areaValue = areaValue;
    }

    public String getAreaLevel() {
        return areaLevel;
    }

    public void setAreaLevel(String areaLevel) {
        this.areaLevel = areaLevel;
    }

    public Short getStressValue() {
        return stressValue;
    }

    public void setStressValue(Short stressValue) {
        this.stressValue = stressValue;
    }

    public Short getQuakeValue() {
        return quakeValue;
    }

    public void setQuakeValue(Short quakeValue) {
        this.quakeValue = quakeValue;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

	@Override
	public String toString() {
		return "AreaDataInfo [areaId=" + areaId + ", acquisitionTime=" + acquisitionTime + ", areaValue=" + areaValue
				+ ", areaLevel=" + areaLevel + ", stressValue=" + stressValue + ", quakeValue=" + quakeValue + ", memo="
				+ memo + "]";
	}
}