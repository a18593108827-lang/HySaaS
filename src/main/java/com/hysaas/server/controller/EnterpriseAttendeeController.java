package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.system.dto.EnterpriseAttendeePayload;
import com.hysaas.system.dto.EnterpriseAttendeeVO;
import com.hysaas.system.service.EnterpriseAttendeeService;
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
@RequestMapping("/enterprise/attendees")
@RequiredArgsConstructor
@SaCheckRole("ENTERPRISE")
public class EnterpriseAttendeeController {

    private final EnterpriseAttendeeService enterpriseAttendeeService;

    @GetMapping
    public R<PageResult<EnterpriseAttendeeVO>> list(@RequestParam(required = false) String nickname,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer size) {
        return R.ok(enterpriseAttendeeService.page(nickname, page, size));
    }

    @GetMapping("/{id}")
    public R<EnterpriseAttendeeVO> detail(@PathVariable Long id) {
        return R.ok(enterpriseAttendeeService.getById(id));
    }

    @PostMapping
    public R<EnterpriseAttendeeVO> create(@Valid @RequestBody EnterpriseAttendeePayload payload) {
        return R.ok(enterpriseAttendeeService.create(payload));
    }

    @PutMapping("/{id}")
    public R<EnterpriseAttendeeVO> update(@PathVariable Long id, @RequestBody EnterpriseAttendeePayload payload) {
        return R.ok(enterpriseAttendeeService.update(id, payload));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        enterpriseAttendeeService.delete(id);
        return R.ok();
    }
}
