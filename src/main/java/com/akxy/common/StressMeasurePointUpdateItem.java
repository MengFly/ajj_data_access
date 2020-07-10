package com.akxy.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StressMeasurePointUpdateItem {

    private Long id;
    private String name;
    private String toTime;
    private String fromTime;
    private long lToTime;
    private long lFromTime;
}
