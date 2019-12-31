package com.akxy.entity;

public class Config {
    private String type;

    private String name;

    private Short sequence;

    private String strValue;

    private Short intValue;

    private String memo;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Short getSequence() {
        return sequence;
    }

    public void setSequence(Short sequence) {
        this.sequence = sequence;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue == null ? null : strValue.trim();
    }

    public Short getIntValue() {
        return intValue;
    }

    public void setIntValue(Short intValue) {
        this.intValue = intValue;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }
}