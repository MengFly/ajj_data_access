package com.akxy.entity;

import java.util.Date;

public class StressDataInfoKey {
    private Short mpId;

    private Date acquisitionTime;

    private Short areaId;

    public Short getMpId() {
        return mpId;
    }

    public void setMpId(Short mpId) {
        this.mpId = mpId;
    }

    public Date getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(Date acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public Short getAreaId() {
        return areaId;
    }

    public void setAreaId(Short areaId) {
        this.areaId = areaId;
    }
}