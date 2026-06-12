package com.hysaas.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_event_permission")
public class SysEventPermission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long eventId;
    private Long userId;
    private String permCode;
    private LocalDateTime createdAt;
}
