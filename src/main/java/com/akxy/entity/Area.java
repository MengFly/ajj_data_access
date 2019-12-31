package com.akxy.entity;

public class Area {
    private Long id;

    private String name;

    private String posList;

    private Short ismonitor;

    private String memo;

    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPosList() {
        return posList;
    }

    public void setPosList(String posList) {
        this.posList = posList == null ? null : posList.trim();
    }

    public Short getIsmonitor() {
        return ismonitor;
    }

    public void setIsmonitor(Short ismonitor) {
        this.ismonitor = ismonitor;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

	@Override
	public String toString() {
		return "Area [id=" + id + ", name=" + name + ", posList=" + posList + ", ismonitor=" + ismonitor + ", memo="
				+ memo + ", type=" + type + "]";
	}
    
}