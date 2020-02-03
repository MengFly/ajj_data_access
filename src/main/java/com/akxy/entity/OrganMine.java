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
public class OrganMine {
    private Short id;

    private String name;

    private Short type;

    private String address;

    private Short pid;

    private String phone;

    private Long longitude;

    private Long latitude;

    private String tabspace;

    private String memo;

    private String level;

    private Long sequence;

    private String operator;

    private String operateIp;

    private Date operateTime;
}