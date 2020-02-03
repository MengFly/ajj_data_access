package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author wangp
 */
@Getter
@Setter
@ToString
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
}