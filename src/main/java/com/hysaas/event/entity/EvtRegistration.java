package com.hysaas.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evt_registration")
public class EvtRegistration {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long eventId;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String memberType;
    private String status;
    private String source;
    private LocalDateTime createdAt;
}
