package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Stress {
    private Integer id;

    private Date collectiontime;

    private String minecode;

    private String areaname;

    private String areatype;

    private String systemname;

    private String systemtype;

    private String stresstype;

    private String rtvpos;

    private Date installtime;

    private String stressno;

    private String stressdir;

    private String tunnelname;

    private String name;

    private Double value;

    private Double x;

    private Double y;

    private Double z;

    private Double depth;

    private Double distance;

    private Double initialvalue;

    private Double redwarn;

    private Double yellowwarn;

    private String warnrecord;

    private String memo;
}