package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.hysaas.common.constant.EnterpriseRoles;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.paper.dto.PaperSubmissionVO;
import com.hysaas.paper.dto.PaperAssignRequest;
import com.hysaas.paper.dto.PaperFinalizeRequest;
import com.hysaas.paper.service.PaperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprise/papers")
@RequiredArgsConstructor
@SaCheckRole(value = {EnterpriseRoles.ADMIN, EnterpriseRoles.EVENT_STAFF}, mode = SaMode.OR)
public class EnterprisePaperController {

    private final PaperService paperService;

    @GetMapping
    public R<PageResult<PaperSubmissionVO>> list(@RequestParam(required = false) String status,
                                                                       @RequestParam(required = false) Integer page,
                                                                       @RequestParam(required = false) Integer size) {
        return R.ok(paperService.enterpriseList(status, page, size));
    }

    @PostMapping("/{id}/assign")
    public R<PaperSubmissionVO> assign(@PathVariable Long id, @RequestBody PaperAssignRequest request) {
        return R.ok(paperService.assign(id, request));
    }

    @PutMapping("/{id}/finalize")
    public R<PaperSubmissionVO> finalizePaper(@PathVariable Long id, @Valid @RequestBody PaperFinalizeRequest request) {
        return R.ok(paperService.finalizePaper(id, request.getStatus()));
    }
}
