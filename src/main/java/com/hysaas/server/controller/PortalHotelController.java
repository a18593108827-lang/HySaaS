package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.hotel.dto.PortalBookingRequest;
import com.hysaas.hotel.dto.PortalHotelRoomVO;
import com.hysaas.hotel.service.HotelService;
import com.hysaas.payment.dto.PayOrderVO;
import com.hysaas.payment.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/hotels")
@RequiredArgsConstructor
@SaCheckRole("ATTENDEE")
public class PortalHotelController {

    private final HotelService hotelService;
    private final PayOrderService payOrderService;

    @GetMapping("/{eventId}")
    public R<List<PortalHotelRoomVO>> rooms(@PathVariable Long eventId) {
        return R.ok(hotelService.portalRooms(eventId));
    }

    @PostMapping("/{eventId}/book")
    public R<PayOrderVO> book(@PathVariable Long eventId, @RequestBody PortalBookingRequest request) {
        return R.ok(payOrderService.toOrderVO(hotelService.portalBook(eventId, request)));
    }
}
