package com.hysaas.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotelRoomTypePayload {

    @NotBlank(message = "房型名称不能为空")
    private String name;

    @NotNull(message = "协议价不能为空")
    private BigDecimal price;

    @NotNull(message = "配额不能为空")
    private Integer quota;
}
