package com.akxy.entity;

import java.math.BigDecimal;
import java.util.Date;

public class StressTopDataInfo {
    private Date acquisitionTime;

    private String mpName;

    private Long areaId;

    private Double pValue;

    private Double x;

    private Double y;

    private Double z;

    private Short warnStatus;

    private BigDecimal redValue;

    private BigDecimal yellowValue;

    private BigDecimal distance;

    private BigDecimal depth;

    private String memo;

    private Long mpId;

    private Double pIndex;

    private Double dayIncreaseIndex;

    private Double zsIndex;

    private Double zfIndex;

    private Double singleIndex;

    public Date getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(Date acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public String getMpName() {
        return mpName;
    }

    public void setMpName(String mpName) {
        this.mpName = mpName == null ? null : mpName.trim();
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Double getpValue() {
        return pValue;
    }

    public void setpValue(Double pValue) {
        this.pValue = pValue;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public Short getWarnStatus() {
        return warnStatus;
    }

    public void setWarnStatus(Short warnStatus) {
        this.warnStatus = warnStatus;
    }

    public BigDecimal getRedValue() {
        return redValue;
    }

    public void setRedValue(BigDecimal redValue) {
        this.redValue = redValue;
    }

    public BigDecimal getYellowValue() {
        return yellowValue;
    }

    public void setYellowValue(BigDecimal yellowValue) {
        this.yellowValue = yellowValue;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public BigDecimal getDepth() {
        return depth;
    }

    public void setDepth(BigDecimal depth) {
        this.depth = depth;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public Long getMpId() {
        return mpId;
    }

    public void setMpId(Long mpId) {
        this.mpId = mpId;
    }

    public Double getpIndex() {
        return pIndex;
    }

    public void setpIndex(Double pIndex) {
        this.pIndex = pIndex;
    }

    public Double getDayIncreaseIndex() {
        return dayIncreaseIndex;
    }

    public void setDayIncreaseIndex(Double dayIncreaseIndex) {
        this.dayIncreaseIndex = dayIncreaseIndex;
    }

    public Double getZsIndex() {
        return zsIndex;
    }

    public void setZsIndex(Double zsIndex) {
        this.zsIndex = zsIndex;
    }

    public Double getZfIndex() {
        return zfIndex;
    }

    public void setZfIndex(Double zfIndex) {
        this.zfIndex = zfIndex;
    }

    public Double getSingleIndex() {
        return singleIndex;
    }

    public void setSingleIndex(Double singleIndex) {
        this.singleIndex = singleIndex;
    }

	@Override
	public String toString() {
		return "StressTopDataInfo [acquisitionTime=" + acquisitionTime + ", mpName=" + mpName + ", areaId=" + areaId
				+ ", pValue=" + pValue + ", x=" + x + ", y=" + y + ", z=" + z + ", warnStatus=" + warnStatus
				+ ", redValue=" + redValue + ", yellowValue=" + yellowValue + ", distance=" + distance + ", depth="
				+ depth + ", memo=" + memo + ", mpId=" + mpId + ", pIndex=" + pIndex + ", dayIncreaseIndex="
				+ dayIncreaseIndex + ", zsIndex=" + zsIndex + ", zfIndex=" + zfIndex + ", singleIndex=" + singleIndex
				+ "]";
	}
}