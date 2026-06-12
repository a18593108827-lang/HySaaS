package com.hysaas.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckinListVO {

    private int count;
    private int total;
    private List<CheckinRecordVO> records;
}
