package com.hysaas.hotel.dto;

import lombok.Data;

@Data
public class PortalHotelRoomVO {

    private Long id;
    private String name;
    private java.math.BigDecimal price;
    private Integer quota;
}
