package com.hysaas.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pay_transaction")
public class PayTransaction {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private String tradeNo;
    private String alipayTradeNo;
    private String status;
    private String notifyData;
    private LocalDateTime createdAt;
}
