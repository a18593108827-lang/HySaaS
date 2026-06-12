package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.paper.dto.ReviewSubmitRequest;
import com.hysaas.paper.dto.ReviewTaskVO;
import com.hysaas.paper.service.PaperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/enterprise/reviews")
@RequiredArgsConstructor
@SaCheckRole("ENTERPRISE")
public class EnterpriseReviewController {

    private final PaperService paperService;

    @GetMapping
    public R<List<ReviewTaskVO>> tasks() {
        return R.ok(paperService.reviewTasks());
    }

    @PostMapping("/{paperId}")
    public R<Void> submit(@PathVariable Long paperId, @Valid @RequestBody ReviewSubmitRequest request) {
        paperService.submitReview(paperId, request);
        return R.ok();
    }
}
