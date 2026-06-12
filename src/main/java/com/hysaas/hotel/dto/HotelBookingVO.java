package com.hysaas.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HotelBookingVO {

    private Long id;
    private String bookingNo;
    private Long eventId;
    private String eventTitle;
    private String guestName;
    private String hotelName;
    private String roomTypeName;
    private Integer nights;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime checkedInAt;
}
