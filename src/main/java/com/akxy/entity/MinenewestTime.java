package com.akxy.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * MINENEWESTTIME
 * @author wangp
 */
@Data
public class MinenewestTime implements Serializable {
    private String mineCode;

    private String mineName;

    private Date newestTime;

    private static final long serialVersionUID = 1L;
}