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
public class StressDataInfo{
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

    private String memo;

    private Long mpId;

    private Double dayIncreaseIndex;

    private Double zsIndex;

    private Double zfIndex;

    private Double singleIndex;

    public Date getAcquisitionTime() {
        return acquisitionTime;
    }

	private BigDecimal depth;

    private Double pIndex;


    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof StressDataInfo) {
			StressDataInfo stressDataInfo=(StressDataInfo) obj;
			return this.acquisitionTime.equals(stressDataInfo.acquisitionTime) && 
					this.areaId.equals(stressDataInfo.areaId) && 
					this.mpId.equals(stressDataInfo.mpId);
		}
    	return super.equals(obj);
    }
}