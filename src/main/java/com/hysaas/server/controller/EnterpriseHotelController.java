package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.hotel.dto.HotelInfoPayload;
import com.hysaas.hotel.dto.HotelInfoVO;
import com.hysaas.hotel.dto.HotelRoomTypePayload;
import com.hysaas.hotel.dto.HotelRoomTypeVO;
import com.hysaas.hotel.service.HotelService;
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

import java.util.List;

@RestController
@RequestMapping("/enterprise/hotels")
@RequiredArgsConstructor
@SaCheckRole("ENTERPRISE")
public class EnterpriseHotelController {

    private final HotelService hotelService;

    @GetMapping
    public R<List<HotelInfoVO>> list() {
        return R.ok(hotelService.listHotels());
    }

    @GetMapping("/{id}")
    public R<HotelInfoVO> detail(@PathVariable Long id) {
        return R.ok(hotelService.getHotel(id));
    }

    @PostMapping
    public R<HotelInfoVO> create(@Valid @RequestBody HotelInfoPayload payload) {
        return R.ok(hotelService.createHotel(payload));
    }

    @PutMapping("/{id}")
    public R<HotelInfoVO> update(@PathVariable Long id, @RequestBody HotelInfoPayload payload) {
        return R.ok(hotelService.updateHotel(id, payload));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return R.ok();
    }

    @GetMapping("/{hotelId}/room-types")
    public R<List<HotelRoomTypeVO>> roomTypes(@PathVariable Long hotelId) {
        return R.ok(hotelService.listRoomTypes(hotelId));
    }

    @PostMapping("/{hotelId}/room-types")
    public R<HotelRoomTypeVO> createRoomType(@PathVariable Long hotelId,
                                             @Valid @RequestBody HotelRoomTypePayload payload) {
        return R.ok(hotelService.createRoomType(hotelId, payload));
    }

    @PutMapping("/{hotelId}/room-types/{id}")
    public R<HotelRoomTypeVO> updateRoomType(@PathVariable Long hotelId, @PathVariable Long id,
                                             @RequestBody HotelRoomTypePayload payload) {
        return R.ok(hotelService.updateRoomType(hotelId, id, payload));
    }

    @DeleteMapping("/{hotelId}/room-types/{id}")
    public R<Void> deleteRoomType(@PathVariable Long hotelId, @PathVariable Long id) {
        hotelService.deleteRoomType(hotelId, id);
        return R.ok();
    }
}
