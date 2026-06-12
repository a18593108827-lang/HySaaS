package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.event.dto.EventItemVO;
import com.hysaas.event.dto.EventSaveRequest;
import com.hysaas.event.service.EventService;
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
@RequestMapping("/enterprise/events")
@RequiredArgsConstructor
@SaCheckRole("ENTERPRISE")
public class EnterpriseEventController {

    private final EventService eventService;

    @GetMapping
    public R<PageResult<EventItemVO>> list(@RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        return R.ok(eventService.page(page, size));
    }

    @GetMapping("/{id}")
    public R<EventItemVO> detail(@PathVariable Long id) {
        return R.ok(eventService.getById(id));
    }

    @PostMapping
    public R<EventItemVO> create(@Valid @RequestBody EventSaveRequest request) {
        return R.ok(eventService.create(request));
    }

    @PutMapping("/{id}")
    public R<EventItemVO> update(@PathVariable Long id, @RequestBody EventSaveRequest request) {
        return R.ok(eventService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/publish")
    public R<EventItemVO> publish(@PathVariable Long id) {
        return R.ok(eventService.publish(id));
    }
}
