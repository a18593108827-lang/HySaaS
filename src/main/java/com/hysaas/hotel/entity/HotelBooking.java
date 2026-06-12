package com.hysaas.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hotel_booking")
public class HotelBooking {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long eventId;
    private Long userId;
    private Long roomTypeId;
    private String bookingNo;
    private String status;
    private BigDecimal amount;
    private Integer nights;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime checkedInAt;
}
