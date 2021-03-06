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
public class ConnStatus {
    private String mineName;

    private String mineCode;

    private String type;

    private String connStatus;

    private String warningDetail;

    private Date acquireTime;

    private String memo;
    
}