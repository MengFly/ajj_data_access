package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class QuakeCopy {
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

    private BigDecimal x;

    private BigDecimal y;

    private BigDecimal z;

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