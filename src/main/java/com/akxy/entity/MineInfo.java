package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author wangp
 */
@Setter
@Getter
@ToString
public class MineInfo {

    private Long areaId;

    private Short type;
    private Long mpId;
    private Date acquisitionTime;

    private Double stressValue;

    private Double quakeValue;

    private String memo;
}