package com.akxy.entity;

import java.util.Date;

public class StressCopy {
	private Integer id;

    private Date collectiontime;

    private String minecode;

    private String memo;
	
    private String areaname;

    private String areatype;

    private String systemname;

    private String systemtype;

    private String stresstype;

    private String rtvpos;

    private String installtime;

    private String stressno;

    private String stressdir;

    private String tunnelname;

    private String name;

    private Float value;

    private Double x;

    private Double y;

    private Double z;

    private Float depth;

    private Float distance;

    private Float initialvalue;

    private Float redwarn;

    private Float yellowwarn;

    private String warnrecord;

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

    public String getInstalltime() {
        return installtime;
    }

    public void setInstalltime(String installtime) {
        this.installtime = installtime == null ? null : installtime.trim();
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

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
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

    public Float getDepth() {
        return depth;
    }

    public void setDepth(Float depth) {
        this.depth = depth;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Float getInitialvalue() {
        return initialvalue;
    }

    public void setInitialvalue(Float initialvalue) {
        this.initialvalue = initialvalue;
    }

    public Float getRedwarn() {
        return redwarn;
    }

    public void setRedwarn(Float redwarn) {
        this.redwarn = redwarn;
    }

    public Float getYellowwarn() {
        return yellowwarn;
    }

    public void setYellowwarn(Float yellowwarn) {
        this.yellowwarn = yellowwarn;
    }

    public String getWarnrecord() {
        return warnrecord;
    }

    public void setWarnrecord(String warnrecord) {
        this.warnrecord = warnrecord == null ? null : warnrecord.trim();
    }
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

	@Override
	public String toString() {
		return "StressCopy [id=" + id + ", collectiontime=" + collectiontime + ", minecode=" + minecode + ", memo="
				+ memo + ", areaname=" + areaname + ", areatype=" + areatype + ", systemname=" + systemname
				+ ", systemtype=" + systemtype + ", stresstype=" + stresstype + ", rtvpos=" + rtvpos + ", installtime="
				+ installtime + ", stressno=" + stressno + ", stressdir=" + stressdir + ", tunnelname=" + tunnelname
				+ ", name=" + name + ", value=" + value + ", x=" + x + ", y=" + y + ", z=" + z + ", depth=" + depth
				+ ", distance=" + distance + ", initialvalue=" + initialvalue + ", redwarn=" + redwarn + ", yellowwarn="
				+ yellowwarn + ", warnrecord=" + warnrecord + "]";
	}
    
}