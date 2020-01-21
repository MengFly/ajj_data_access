package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class AreaTopDataInfo {
    private Long areaId;

    private Date acquisitionTime;

    private Short areaValue;

    private String areaLevel;

    private Short stressValue;

    private Short quakeValue;

    private String memo;
}