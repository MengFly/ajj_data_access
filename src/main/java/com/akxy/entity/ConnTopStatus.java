package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ConnTopStatus {
    private String mineName;

    private String mineCode;

    private String type;

    private String connStatus;

    private String warningDetail;

    private Date acquireTime;

    private String memo;
}