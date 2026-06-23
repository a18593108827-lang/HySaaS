package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.hysaas.common.constant.EnterpriseRoles;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.event.dto.CheckinListVO;
import com.hysaas.event.dto.CheckinQrcodeResultVO;
import com.hysaas.event.dto.EventInviteLinkResultVO;
import com.hysaas.event.dto.EventInviteRequest;
import com.hysaas.event.dto.EventInviteResultVO;
import com.hysaas.event.dto.EventItemVO;
import com.hysaas.event.dto.EventSaveRequest;
import com.hysaas.event.dto.RegistrationVO;
import com.hysaas.event.service.CheckinService;
import com.hysaas.event.service.EventService;
import com.hysaas.event.service.RegistrationService;
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
@SaCheckRole(value = {EnterpriseRoles.ADMIN, EnterpriseRoles.EVENT_STAFF}, mode = SaMode.OR)
public class EnterpriseEventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CheckinService checkinService;

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

    @GetMapping("/{eventId}/registrations")
    public R<PageResult<RegistrationVO>> registrations(@PathVariable Long eventId,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        return R.ok(registrationService.pageByEvent(eventId, status, page, size));
    }

    @GetMapping("/{eventId}/checkin")
    public R<CheckinListVO> checkinList(@PathVariable Long eventId) {
        return R.ok(checkinService.enterpriseList(eventId));
    }

    @PostMapping("/{id}/qrcode")
    public R<CheckinQrcodeResultVO> qrcode(@PathVariable Long id) {
        return R.ok(checkinService.generateQrcode(id));
    }

    @PostMapping("/{eventId}/checkin/registrations/{registrationId}")
    public R<Void> proxyCheckin(@PathVariable Long eventId, @PathVariable Long registrationId) {
        checkinService.enterpriseProxyCheckin(eventId, registrationId);
        return R.ok();
    }

    @PostMapping("/{id}/invites")
    public R<EventInviteResultVO> invites(@PathVariable Long id, @RequestBody EventInviteRequest request) {
        return R.ok(registrationService.invite(id, request));
    }

    @PostMapping("/{id}/invite-link")
    public R<EventInviteLinkResultVO> inviteLink(@PathVariable Long id) {
        return R.ok(registrationService.generateInviteLink(id));
    }
}
