package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wangp
 */
@Getter
@Setter
@ToString
public class Config {
    private String type;

    private String name;

    private Short sequence;

    private String strValue;

    private Short intValue;

    private String memo;
}