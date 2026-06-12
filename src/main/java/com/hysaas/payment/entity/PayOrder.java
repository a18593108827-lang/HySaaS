package com.hysaas.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_order")
public class PayOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNo;
    private Long tenantId;
    private Long userId;
    private String bizType;
    private Long bizId;
    private BigDecimal amount;
    private String status;
    private String invoiceStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
