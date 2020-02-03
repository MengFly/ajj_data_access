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
public class StressMeasurePoint {
    private Long id;

    private Long areaId;

    private String name;

    private String tunnelName;

    private Double x;

    private Double y;

    private Double z;

    private Double depth;

    private Double distance;

    private Double initialValue;

    private Double yellowWarnvalue;

    private Double redWarnvalue;

    private String memo;

    private Date fromTime;

    private Date toTime;

    
}