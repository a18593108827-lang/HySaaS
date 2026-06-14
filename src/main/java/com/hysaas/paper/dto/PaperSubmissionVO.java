package com.hysaas.paper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaperSubmissionVO {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private String title;
    private String author;

    @JsonProperty("abstract")
    private String abstractText;

    private String fileKey;
    private String status;
    private Integer version;
    private LocalDateTime submittedAt;
}
