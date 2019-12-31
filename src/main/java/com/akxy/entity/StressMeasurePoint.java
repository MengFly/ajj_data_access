package com.akxy.entity;

import java.util.Date;

public class StressMeasurePoint {
    private Long id;

    private Long areaId;

    private String name;

    private String tunnelName;

    private Double x;

    private Double y;

    private Double z;

    private Double depth;

    private Double distance;

    private Double initialValue;

    private Double yellowWarnvalue;

    private Double redWarnvalue;

    private String memo;

    private Date fromTime;

    private Date toTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getTunnelName() {
        return tunnelName;
    }

    public void setTunnelName(String tunnelName) {
        this.tunnelName = tunnelName == null ? null : tunnelName.trim();
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

    public Double getDepth() {
        return depth;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(Double initialValue) {
        this.initialValue = initialValue;
    }

    public Double getYellowWarnvalue() {
        return yellowWarnvalue;
    }

    public void setYellowWarnvalue(Double yellowWarnvalue) {
        this.yellowWarnvalue = yellowWarnvalue;
    }

    public Double getRedWarnvalue() {
        return redWarnvalue;
    }

    public void setRedWarnvalue(Double redWarnvalue) {
        this.redWarnvalue = redWarnvalue;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

	@Override
	public String toString() {
		return "StressMeasurePoint [id=" + id + ", areaId=" + areaId + ", name=" + name + ", tunnelName=" + tunnelName
				+ ", x=" + x + ", y=" + y + ", z=" + z + ", depth=" + depth + ", distance=" + distance
				+ ", initialValue=" + initialValue + ", yellowWarnvalue=" + yellowWarnvalue + ", redWarnvalue="
				+ redWarnvalue + ", memo=" + memo + ", fromTime=" + fromTime + ", toTime=" + toTime + "]";
	}
    
}