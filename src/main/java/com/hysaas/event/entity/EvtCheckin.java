package com.hysaas.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evt_checkin")
public class EvtCheckin {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long eventId;
    private Long userId;
    private String source;
    private LocalDateTime checkinAt;
}
