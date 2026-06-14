package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.paper.dto.PaperSubmissionVO;
import com.hysaas.paper.dto.PaperSubmitRequest;
import com.hysaas.paper.dto.PortalDraftRequest;
import jakarta.validation.Valid;
import com.hysaas.paper.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/portal/submissions")
@RequiredArgsConstructor
@SaCheckRole("ATTENDEE")
public class PortalSubmissionController {

    private final PaperService paperService;

    @GetMapping
    public R<List<PaperSubmissionVO>> list(@RequestParam(defaultValue = "draft") String scope) {
        return R.ok(paperService.portalList(scope));
    }

    @PostMapping("/draft")
    public R<PaperSubmissionVO> draft(@RequestBody PortalDraftRequest request) {
        return R.ok(paperService.saveDraft(request));
    }

    @PostMapping("/{id}/file")
    public R<PaperSubmissionVO> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return R.ok(paperService.uploadFile(id, file));
    }

    @PostMapping("/{id}/submit")
    public R<PaperSubmissionVO> submit(@PathVariable Long id, @RequestBody(required = false) @Valid PaperSubmitRequest request) {
        return R.ok(paperService.submitPaper(id, request));
    }
}
