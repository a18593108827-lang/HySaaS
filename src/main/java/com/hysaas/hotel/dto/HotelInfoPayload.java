package com.hysaas.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HotelInfoPayload {

    @NotBlank(message = "酒店名称不能为空")
    private String name;
    private String address;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
}
