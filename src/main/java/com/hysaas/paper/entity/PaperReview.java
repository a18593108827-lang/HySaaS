package com.hysaas.paper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("paper_review")
public class PaperReview {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long submissionId;
    private Long reviewerId;
    private String comment;
    private String suggest;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
