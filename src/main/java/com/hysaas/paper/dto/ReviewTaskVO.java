package com.hysaas.paper.dto;

import lombok.Data;

@Data
public class ReviewTaskVO {

    private Long paperId;
    private String title;
    private String author;
    private String deadline;
}
