package com.akxy.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Quake {
    private Integer id;

    private Date collectiontime;

    private String minecode;

    private String areaname;

    private Short chcount;

    private Short samplength;

    private Short sampfreq;

    private String sensordir;

    private String installway;

    private Double sensitivity;

    private String sensortype;

    private Double x;

    private Double y;

    private Double z;

    private Double energy;

    private Double level;

    private String posdesc;

    private Double maxswing;

    private Double avgswing;

    private Double basicfreq;

    private String trigch;

    private String excstatus;

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

    public Short getChcount() {
        return chcount;
    }

    public void setChcount(Short chcount) {
        this.chcount = chcount;
    }

    public Short getSamplength() {
        return samplength;
    }

    public void setSamplength(Short samplength) {
        this.samplength = samplength;
    }

    public Short getSampfreq() {
        return sampfreq;
    }

    public void setSampfreq(Short sampfreq) {
        this.sampfreq = sampfreq;
    }

    public String getSensordir() {
        return sensordir;
    }

    public void setSensordir(String sensordir) {
        this.sensordir = sensordir == null ? null : sensordir.trim();
    }

    public String getInstallway() {
        return installway;
    }

    public void setInstallway(String installway) {
        this.installway = installway == null ? null : installway.trim();
    }

    public Double getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(Double sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getSensortype() {
        return sensortype;
    }

    public void setSensortype(String sensortype) {
        this.sensortype = sensortype == null ? null : sensortype.trim();
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

    public Double getEnergy() {
        return energy;
    }

    public void setEnergy(Double energy) {
        this.energy = energy;
    }

    public Double getLevel() {
        return level;
    }

    public void setLevel(Double level) {
        this.level = level;
    }

    public String getPosdesc() {
        return posdesc;
    }

    public void setPosdesc(String posdesc) {
        this.posdesc = posdesc == null ? null : posdesc.trim();
    }

    public Double getMaxswing() {
        return maxswing;
    }

    public void setMaxswing(Double maxswing) {
        this.maxswing = maxswing;
    }

    public Double getAvgswing() {
        return avgswing;
    }

    public void setAvgswing(Double avgswing) {
        this.avgswing = avgswing;
    }

    public Double getBasicfreq() {
        return basicfreq;
    }

    public void setBasicfreq(Double basicfreq) {
        this.basicfreq = basicfreq;
    }

    public String getTrigch() {
        return trigch;
    }

    public void setTrigch(String trigch) {
        this.trigch = trigch == null ? null : trigch.trim();
    }

    public String getExcstatus() {
        return excstatus;
    }

    public void setExcstatus(String excstatus) {
        this.excstatus = excstatus == null ? null : excstatus.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

	@Override
	public String toString() {
		return "Quake [id=" + id + ", collectiontime=" + collectiontime + ", minecode=" + minecode + ", areaname="
				+ areaname + ", chcount=" + chcount + ", samplength=" + samplength + ", sampfreq=" + sampfreq
				+ ", sensordir=" + sensordir + ", installway=" + installway + ", sensitivity=" + sensitivity
				+ ", sensortype=" + sensortype + ", x=" + x + ", y=" + y + ", z=" + z + ", energy=" + energy
				+ ", level=" + level + ", posdesc=" + posdesc + ", maxswing=" + maxswing + ", avgswing=" + avgswing
				+ ", basicfreq=" + basicfreq + ", trigch=" + trigch + ", excstatus=" + excstatus + ", memo=" + memo
				+ "]";
	}
}