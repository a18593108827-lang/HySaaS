package com.hysaas.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotelRoomTypeVO {

    private Long id;
    private Long hotelId;
    private String name;
    private BigDecimal price;
    private Integer quota;
    private Integer used;
}
