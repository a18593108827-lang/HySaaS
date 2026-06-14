package com.hysaas.paper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaperSubmitRequest {

    @NotNull(message = "请选择投稿活动")
    private Long eventId;
}
