package com.akxy.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Stress {
    private Integer id;

    private Date collectiontime;

    private String minecode;

    private String areaname;

    private String areatype;

    private String systemname;

    private String systemtype;

    private String stresstype;

    private String rtvpos;

    private Date installtime;

    private String stressno;

    private String stressdir;

    private String tunnelname;

    private String name;

    private Double value;

    private Double x;

    private Double y;

    private Double z;

    private Double depth;

    private Double distance;

    private Double initialvalue;

    private Double redwarn;

    private Double yellowwarn;

    private String warnrecord;

    private String memo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCollectiontime() {
        return collectiontime;
    }

    public void setCollectiontime(Date collectiontime) {
        this.collectiontime = collectiontime;
    }

    public String getMinecode() {
        return minecode;
    }

    public void setMinecode(String minecode) {
        this.minecode = minecode == null ? null : minecode.trim();
    }

    public String getAreaname() {
        return areaname;
    }

    public void setAreaname(String areaname) {
        this.areaname = areaname == null ? null : areaname.trim();
    }

    public String getAreatype() {
        return areatype;
    }

    public void setAreatype(String areatype) {
        this.areatype = areatype == null ? null : areatype.trim();
    }

    public String getSystemname() {
        return systemname;
    }

    public void setSystemname(String systemname) {
        this.systemname = systemname == null ? null : systemname.trim();
    }

    public String getSystemtype() {
        return systemtype;
    }

    public void setSystemtype(String systemtype) {
        this.systemtype = systemtype == null ? null : systemtype.trim();
    }

    public String getStresstype() {
        return stresstype;
    }

    public void setStresstype(String stresstype) {
        this.stresstype = stresstype == null ? null : stresstype.trim();
    }

    public String getRtvpos() {
        return rtvpos;
    }

    public void setRtvpos(String rtvpos) {
        this.rtvpos = rtvpos == null ? null : rtvpos.trim();
    }

    public Date getInstalltime() {
        return installtime;
    }

    public void setInstalltime(Date installtime) {
        this.installtime = installtime;
    }

    public String getStressno() {
        return stressno;
    }

    public void setStressno(String stressno) {
        this.stressno = stressno == null ? null : stressno.trim();
    }

    public String getStressdir() {
        return stressdir;
    }

    public void setStressdir(String stressdir) {
        this.stressdir = stressdir == null ? null : stressdir.trim();
    }

    public String getTunnelname() {
        return tunnelname;
    }

    public void setTunnelname(String tunnelname) {
        this.tunnelname = tunnelname == null ? null : tunnelname.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
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

    public Double getInitialvalue() {
        return initialvalue;
    }

    public void setInitialvalue(Double initialvalue) {
        this.initialvalue = initialvalue;
    }

    public Double getRedwarn() {
        return redwarn;
    }

    public void setRedwarn(Double redwarn) {
        this.redwarn = redwarn;
    }

    public Double getYellowwarn() {
        return yellowwarn;
    }

    public void setYellowwarn(Double yellowwarn) {
        this.yellowwarn = yellowwarn;
    }

    public String getWarnrecord() {
        return warnrecord;
    }

    public void setWarnrecord(String warnrecord) {
        this.warnrecord = warnrecord == null ? null : warnrecord.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

	@Override
	public String toString() {
		return "Stress [id=" + id + ", collectiontime=" + collectiontime + ", minecode=" + minecode + ", areaname="
				+ areaname + ", areatype=" + areatype + ", systemname=" + systemname + ", systemtype=" + systemtype
				+ ", stresstype=" + stresstype + ", rtvpos=" + rtvpos + ", installtime=" + installtime + ", stressno="
				+ stressno + ", stressdir=" + stressdir + ", tunnelname=" + tunnelname + ", name=" + name + ", value="
				+ value + ", x=" + x + ", y=" + y + ", z=" + z + ", depth=" + depth + ", distance=" + distance
				+ ", initialvalue=" + initialvalue + ", redwarn=" + redwarn + ", yellowwarn=" + yellowwarn
				+ ", warnrecord=" + warnrecord + ", memo=" + memo + "]";
	}
}