package com.hysaas.paper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewSubmitRequest {

    @NotBlank(message = "评审意见不能为空")
    private String comment;
    private String suggest;
}
