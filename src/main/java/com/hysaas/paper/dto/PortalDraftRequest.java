package com.hysaas.paper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PortalDraftRequest {

    private Long id;

    private String title;

    private String author;

    @JsonProperty("abstract")
    private String abstractText;
}
