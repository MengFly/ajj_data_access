package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class StressCopy {
	private Integer id;

    private Date collectiontime;

    private String minecode;

    private String memo;
	
    private String areaname;

    private String areatype;

    private String systemname;

    private String systemtype;

    private String stresstype;

    private String rtvpos;

    private String installtime;

    private String stressno;

    private String stressdir;

    private String tunnelname;

    private String name;

    private Float value;

    private Double x;

    private Double y;

    private Double z;

    private Float depth;

    private Float distance;

    private Float initialvalue;

    private Float redwarn;

    private Float yellowwarn;

    private String warnrecord;

    
}