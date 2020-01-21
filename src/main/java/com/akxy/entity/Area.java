package com.akxy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class Area {
    private Long id;

    private String name;

    private String posList;

    private Long monitor;

    private String memo;

    private String type;

    public static Area newInstance(Long id, String name, String type, String memo) {
        Area area = new Area();
        area.setId(id);
        area.setName(name);
        area.setPosList("0");
        area.setMonitor(1L);
        area.setType(type);
        area.setMemo(memo);
        return area;
    }


}