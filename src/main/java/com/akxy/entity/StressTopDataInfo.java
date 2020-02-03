package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author wangp
 */
@Getter
@Setter
@ToString
public class StressTopDataInfo {
    private Date acquisitionTime;

    private String mpName;

    private Long areaId;

    private Double pValue;

    private Double x;

    private Double y;

    private Double z;

    private Short warnStatus;

    private BigDecimal redValue;

    private BigDecimal yellowValue;

    private BigDecimal distance;

    private BigDecimal depth;

    private String memo;

    private Long mpId;

    private Double pIndex;

    private Double dayIncreaseIndex;

    private Double zsIndex;

    private Double zfIndex;

    private Double singleIndex;

}