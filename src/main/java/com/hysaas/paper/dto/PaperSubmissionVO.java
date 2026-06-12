package com.hysaas.paper.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaperSubmissionVO {

    private Long id;
    private String title;
    private String author;
    private String status;
    private Integer version;
    private LocalDateTime submittedAt;
}
