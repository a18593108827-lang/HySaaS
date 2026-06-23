package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.hysaas.common.constant.EnterpriseRoles;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.hotel.dto.HotelBookingVO;
import com.hysaas.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprise/bookings")
@RequiredArgsConstructor
@SaCheckRole(value = {EnterpriseRoles.ADMIN, EnterpriseRoles.EVENT_STAFF, EnterpriseRoles.FINANCE}, mode = SaMode.OR)
public class EnterpriseBookingController {

    private final HotelService hotelService;

    @GetMapping
    public R<PageResult<HotelBookingVO>> list(@RequestParam(required = false) String status,
                                              @RequestParam(required = false) Long eventId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size) {
        return R.ok(hotelService.enterpriseBookings(status, eventId, page, size));
    }

    @PostMapping("/{id}/checkin")
    public R<Void> checkin(@PathVariable Long id) {
        hotelService.verifyBooking(id);
        return R.ok();
    }
}
