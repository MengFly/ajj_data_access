package com.akxy.entity;

import java.util.Date;

public class PosResult {
    private Date collectTime;

    private Double x;

    private Double y;

    private Double z;

    private Double energy;

    private Long areaId;

    private String source;

    private String memo;
    
    private Long chcount;

    private Long samplength;

    private Long sampfreq;

    private String sensordir;

    private String installway;

    private Long sensitivity;

    private String sensortype;

    private String posdesc;

    private Long maxswing;

    private Long avgswing;

    private Long basicfreq;

    private String trigch;

    private String excstatus;

    public Long getChcount() {
		return chcount;
	}

	public void setChcount(Long chcount) {
		this.chcount = chcount;
	}

	public Long getSamplength() {
		return samplength;
	}

	public void setSamplength(Long samplength) {
		this.samplength = samplength;
	}

	public Long getSampfreq() {
		return sampfreq;
	}

	public void setSampfreq(Long sampfreq) {
		this.sampfreq = sampfreq;
	}

	public String getSensordir() {
		return sensordir;
	}

	public void setSensordir(String sensordir) {
		this.sensordir = sensordir;
	}

	public String getInstallway() {
		return installway;
	}

	public void setInstallway(String installway) {
		this.installway = installway;
	}

	public Long getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(Long sensitivity) {
		this.sensitivity = sensitivity;
	}

	public String getSensortype() {
		return sensortype;
	}

	public void setSensortype(String sensortype) {
		this.sensortype = sensortype;
	}

	public String getPosdesc() {
		return posdesc;
	}

	public void setPosdesc(String posdesc) {
		this.posdesc = posdesc;
	}

	public Long getMaxswing() {
		return maxswing;
	}

	public void setMaxswing(Long maxswing) {
		this.maxswing = maxswing;
	}

	public Long getAvgswing() {
		return avgswing;
	}

	public void setAvgswing(Long avgswing) {
		this.avgswing = avgswing;
	}

	public Long getBasicfreq() {
		return basicfreq;
	}

	public void setBasicfreq(Long basicfreq) {
		this.basicfreq = basicfreq;
	}

	public String getTrigch() {
		return trigch;
	}

	public void setTrigch(String trigch) {
		this.trigch = trigch;
	}

	public String getExcstatus() {
		return excstatus;
	}

	public void setExcstatus(String excstatus) {
		this.excstatus = excstatus;
	}

	public Date getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Date collectTime) {
        this.collectTime = collectTime;
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

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

	@Override
	public String toString() {
		return "PosResult [collectTime=" + collectTime + ", x=" + x + ", y=" + y + ", z=" + z + ", energy=" + energy
				+ ", areaId=" + areaId + ", source=" + source + ", memo=" + memo + ", chcount=" + chcount
				+ ", samplength=" + samplength + ", sampfreq=" + sampfreq + ", sensordir=" + sensordir + ", installway="
				+ installway + ", sensitivity=" + sensitivity + ", sensortype=" + sensortype + ", posdesc=" + posdesc
				+ ", maxswing=" + maxswing + ", avgswing=" + avgswing + ", basicfreq=" + basicfreq + ", trigch="
				+ trigch + ", excstatus=" + excstatus + "]";
	}
    
}