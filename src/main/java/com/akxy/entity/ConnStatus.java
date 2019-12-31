package com.akxy.entity;

import java.util.Date;

public class ConnStatus {
    private String mineName;

    private String mineCode;

    private String type;

    private String connStatus;

    private String warningDetail;

    private Date acquireTime;

    private String memo;

    public String getMineName() {
        return mineName;
    }

    public void setMineName(String mineName) {
        this.mineName = mineName == null ? null : mineName.trim();
    }

    public String getMineCode() {
        return mineCode;
    }

    public void setMineCode(String mineCode) {
        this.mineCode = mineCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConnStatus() {
        return connStatus;
    }

    public void setConnStatus(String connStatus) {
        this.connStatus = connStatus;
    }

    public String getWarningDetail() {
        return warningDetail;
    }

    public void setWarningDetail(String warningDetail) {
        this.warningDetail = warningDetail == null ? null : warningDetail.trim();
    }

    public Date getAcquireTime() {
        return acquireTime;
    }

    public void setAcquireTime(Date acquireTime) {
        this.acquireTime = acquireTime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

	@Override
	public String toString() {
		return "ConnStatus [mineName=" + mineName + ", mineCode=" + mineCode + ", type=" + type + ", connStatus="
				+ connStatus + ", warningDetail=" + warningDetail + ", acquireTime=" + acquireTime + ", memo=" + memo
				+ "]";
	}
    
}