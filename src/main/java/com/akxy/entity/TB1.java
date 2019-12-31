package com.akxy.entity;

public class TB1 {
    private Short enumation;

    private Short esum;

    private Short emax;

    private String time2;

    public Short getEnumation() {
        return enumation;
    }

    public void setEnumation(Short enumation) {
        this.enumation = enumation;
    }

    public Short getEsum() {
        return esum;
    }

    public void setEsum(Short esum) {
        this.esum = esum;
    }

    public Short getEmax() {
        return emax;
    }

    public void setEmax(Short emax) {
        this.emax = emax;
    }

    public String getTime2() {
        return time2;
    }

    public void setTime2(String time2) {
        this.time2 = time2 == null ? null : time2.trim();
    }
}