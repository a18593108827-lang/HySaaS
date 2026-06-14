package com.hysaas.paper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PortalDraftRequest {

    private Long eventId;

    private String title;

    @JsonProperty("abstract")
    private String abstractText;
}
