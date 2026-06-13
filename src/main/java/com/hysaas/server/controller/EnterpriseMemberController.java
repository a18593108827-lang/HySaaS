package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.system.dto.EnterpriseMemberPayload;
import com.hysaas.system.dto.EnterpriseMemberVO;
import com.hysaas.system.service.EnterpriseMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprise/members")
@RequiredArgsConstructor
@SaCheckRole("ENTERPRISE")
public class EnterpriseMemberController {

    private final EnterpriseMemberService enterpriseMemberService;

    @GetMapping
    public R<PageResult<EnterpriseMemberVO>> list(@RequestParam(required = false) String role,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size) {
        return R.ok(enterpriseMemberService.page(role, page, size));
    }

    @GetMapping("/{id}")
    public R<EnterpriseMemberVO> detail(@PathVariable Long id) {
        return R.ok(enterpriseMemberService.getById(id));
    }

    @PostMapping
    public R<EnterpriseMemberVO> create(@Valid @RequestBody EnterpriseMemberPayload payload) {
        return R.ok(enterpriseMemberService.create(payload));
    }

    @PutMapping("/{id}")
    public R<EnterpriseMemberVO> update(@PathVariable Long id, @Valid @RequestBody EnterpriseMemberPayload payload) {
        return R.ok(enterpriseMemberService.update(id, payload));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        enterpriseMemberService.delete(id);
        return R.ok();
    }
}
