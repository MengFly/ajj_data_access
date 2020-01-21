package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
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
    
}