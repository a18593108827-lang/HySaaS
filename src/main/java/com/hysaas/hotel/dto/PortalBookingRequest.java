package com.hysaas.hotel.dto;

import lombok.Data;

@Data
public class PortalBookingRequest {

    private Long roomTypeId;
    private Integer nights;
}
