package com.hysaas.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("msg_email_log")
public class MsgEmailLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long eventId;
    private String code;
    private String recipient;
    private String subject;
    private String status;
    private String errorMsg;
    private Integer retryCount;
    private LocalDateTime createdAt;
}
