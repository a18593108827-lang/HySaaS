package com.hysaas.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.hotel.dto.HotelBookingVO;
import com.hysaas.hotel.dto.HotelInfoPayload;
import com.hysaas.hotel.dto.HotelInfoVO;
import com.hysaas.hotel.dto.HotelRoomTypePayload;
import com.hysaas.hotel.dto.HotelRoomTypeVO;
import com.hysaas.hotel.dto.PortalBookingRequest;
import com.hysaas.hotel.dto.PortalHotelRoomVO;
import com.hysaas.hotel.entity.HotelBooking;
import com.hysaas.hotel.entity.HotelInfo;
import com.hysaas.hotel.entity.HotelQuota;
import com.hysaas.hotel.entity.HotelRoomType;
import com.hysaas.hotel.mapper.HotelBookingMapper;
import com.hysaas.hotel.mapper.HotelInfoMapper;
import com.hysaas.hotel.mapper.HotelQuotaMapper;
import com.hysaas.hotel.mapper.HotelRoomTypeMapper;
import com.hysaas.payment.entity.PayOrder;
import com.hysaas.payment.service.PayOrderService;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import com.hysaas.system.support.EnterpriseContext;
import com.hysaas.system.support.PortalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private static final Set<String> PORTAL_EVENT_STATUSES = Set.of("PUBLISHED", "REGISTRATION_OPEN", "REGISTRATION_CLOSED");
    private static final Set<String> ACTIVE_BOOKING_STATUSES = Set.of("PENDING_PAY", "LOCKED", "CHECKED_IN");

    private final HotelInfoMapper hotelInfoMapper;
    private final HotelRoomTypeMapper hotelRoomTypeMapper;
    private final HotelQuotaMapper hotelQuotaMapper;
    private final HotelBookingMapper hotelBookingMapper;
    private final EvtEventMapper evtEventMapper;
    private final SysUserMapper sysUserMapper;
    private final EnterpriseContext enterpriseContext;
    private final PortalContext portalContext;
    private final PayOrderService payOrderService;

    public List<HotelInfoVO> listHotels() {
        enterpriseContext.requireTenantId();
        return hotelInfoMapper.selectList(new LambdaQueryWrapper<HotelInfo>()
                        .orderByDesc(HotelInfo::getCreatedAt))
                .stream().map(this::toHotelVO).toList();
    }

    public HotelInfoVO getHotel(Long id) {
        return toHotelVO(requireHotel(id));
    }

    @Transactional
    public HotelInfoVO createHotel(HotelInfoPayload payload) {
        Long tenantId = enterpriseContext.requireTenantId();
        HotelInfo hotel = new HotelInfo();
        hotel.setTenantId(tenantId);
        hotel.setEventId(resolveDefaultEventId(tenantId));
        hotel.setName(payload.getName());
        hotel.setAddress(payload.getAddress());
        hotel.setContactPhone(payload.getContactPhone());
        hotel.setCreatedAt(LocalDateTime.now());
        hotelInfoMapper.insert(hotel);
        return toHotelVO(hotel);
    }

    @Transactional
    public HotelInfoVO updateHotel(Long id, HotelInfoPayload payload) {
        HotelInfo hotel = requireHotel(id);
        if (StringUtils.hasText(payload.getName())) {
            hotel.setName(payload.getName());
        }
        if (payload.getAddress() != null) {
            hotel.setAddress(payload.getAddress());
        }
        if (StringUtils.hasText(payload.getContactPhone())) {
            hotel.setContactPhone(payload.getContactPhone());
        }
        hotelInfoMapper.updateById(hotel);
        return toHotelVO(hotel);
    }

    @Transactional
    public void deleteHotel(Long id) {
        HotelInfo hotel = requireHotel(id);
        List<HotelRoomType> roomTypes = hotelRoomTypeMapper.selectList(
                new LambdaQueryWrapper<HotelRoomType>().eq(HotelRoomType::getHotelId, hotel.getId()));
        for (HotelRoomType roomType : roomTypes) {
            deleteRoomTypeInternal(hotel.getId(), roomType.getId());
        }
        hotelInfoMapper.deleteById(id);
    }

    public List<HotelRoomTypeVO> listRoomTypes(Long hotelId) {
        requireHotel(hotelId);
        Long eventId = resolveDefaultEventId(enterpriseContext.requireTenantId());
        return hotelRoomTypeMapper.selectList(new LambdaQueryWrapper<HotelRoomType>()
                        .eq(HotelRoomType::getHotelId, hotelId)
                        .orderByDesc(HotelRoomType::getCreatedAt))
                .stream()
                .map(rt -> toRoomTypeVO(rt, eventId))
                .toList();
    }

    @Transactional
    public HotelRoomTypeVO createRoomType(Long hotelId, HotelRoomTypePayload payload) {
        HotelInfo hotel = requireHotel(hotelId);
        Long tenantId = enterpriseContext.requireTenantId();
        HotelRoomType roomType = new HotelRoomType();
        roomType.setTenantId(tenantId);
        roomType.setHotelId(hotelId);
        roomType.setName(payload.getName());
        roomType.setPrice(payload.getPrice());
        roomType.setCreatedAt(LocalDateTime.now());
        hotelRoomTypeMapper.insert(roomType);
        syncQuota(tenantId, roomType.getId(), payload.getQuota());
        return toRoomTypeVO(roomType, resolveDefaultEventId(tenantId));
    }

    @Transactional
    public HotelRoomTypeVO updateRoomType(Long hotelId, Long id, HotelRoomTypePayload payload) {
        requireHotel(hotelId);
        HotelRoomType roomType = requireRoomType(hotelId, id);
        if (StringUtils.hasText(payload.getName())) {
            roomType.setName(payload.getName());
        }
        if (payload.getPrice() != null) {
            roomType.setPrice(payload.getPrice());
        }
        hotelRoomTypeMapper.updateById(roomType);
        if (payload.getQuota() != null) {
            updateQuotaTotal(roomType.getTenantId(), roomType.getId(), payload.getQuota());
        }
        return toRoomTypeVO(roomType, resolveDefaultEventId(roomType.getTenantId()));
    }

    @Transactional
    public void deleteRoomType(Long hotelId, Long id) {
        requireHotel(hotelId);
        deleteRoomTypeInternal(hotelId, id);
    }

    public List<PortalHotelRoomVO> portalRooms(Long eventId) {
        portalContext.requireTenantId();
        requireHotelEvent(eventId);
        List<HotelQuota> quotas = hotelQuotaMapper.selectList(new LambdaQueryWrapper<HotelQuota>()
                .eq(HotelQuota::getEventId, eventId));
        return quotas.stream().map(q -> {
            HotelRoomType roomType = hotelRoomTypeMapper.selectById(q.getRoomTypeId());
            if (roomType == null) {
                return null;
            }
            int remain = q.getTotal() - (int) countActiveBookings(eventId, q.getRoomTypeId());
            if (remain <= 0) {
                return null;
            }
            PortalHotelRoomVO vo = new PortalHotelRoomVO();
            vo.setId(roomType.getId());
            vo.setName(roomType.getName());
            vo.setPrice(roomType.getPrice());
            vo.setQuota(remain);
            return vo;
        }).filter(v -> v != null).toList();
    }

    @Transactional
    public void ensureEventQuotas(Long eventId) {
        EvtEvent event = evtEventMapper.selectById(eventId);
        if (event == null || !intToBool(event.getHotelEnabled())) {
            return;
        }
        Long tenantId = event.getTenantId();
        List<HotelRoomType> roomTypes = hotelRoomTypeMapper.selectList(new LambdaQueryWrapper<HotelRoomType>()
                .eq(HotelRoomType::getTenantId, tenantId));
        for (HotelRoomType roomType : roomTypes) {
            long exists = hotelQuotaMapper.selectCount(new LambdaQueryWrapper<HotelQuota>()
                    .eq(HotelQuota::getEventId, eventId)
                    .eq(HotelQuota::getRoomTypeId, roomType.getId()));
            if (exists > 0) {
                continue;
            }
            HotelQuota template = hotelQuotaMapper.selectOne(new LambdaQueryWrapper<HotelQuota>()
                    .eq(HotelQuota::getRoomTypeId, roomType.getId())
                    .orderByDesc(HotelQuota::getTotal)
                    .last("LIMIT 1"));
            if (template == null) {
                continue;
            }
            HotelQuota quota = new HotelQuota();
            quota.setTenantId(tenantId);
            quota.setEventId(eventId);
            quota.setRoomTypeId(roomType.getId());
            quota.setTotal(template.getTotal());
            quota.setUsed(0);
            hotelQuotaMapper.insert(quota);
        }
    }

    @Transactional
    public PayOrder portalBook(Long eventId, PortalBookingRequest request) {
        if (request.getRoomTypeId() == null) {
            throw new BizException("请选择房型");
        }
        int nights = request.getNights() == null || request.getNights() < 1 ? 1 : request.getNights();
        SysUser user = portalContext.requireAttendee();
        EvtEvent event = requireHotelEvent(eventId);
        HotelRoomType roomType = hotelRoomTypeMapper.selectById(request.getRoomTypeId());
        if (roomType == null) {
            throw new BizException("房型不存在");
        }
        HotelQuota quota = lockQuota(eventId, roomType.getId());
        long active = countActiveBookings(eventId, roomType.getId());
        int remain = quota.getTotal() - (int) active;
        if (remain < 1) {
            throw new BizException("该房型已无剩余配额");
        }
        BigDecimal amount = roomType.getPrice().multiply(BigDecimal.valueOf(nights));
        HotelBooking booking = new HotelBooking();
        booking.setTenantId(event.getTenantId());
        booking.setEventId(eventId);
        booking.setUserId(user.getId());
        booking.setRoomTypeId(roomType.getId());
        booking.setBookingNo(genBookingNo());
        booking.setStatus("PENDING_PAY");
        booking.setAmount(amount);
        booking.setNights(nights);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        hotelBookingMapper.insert(booking);
        return payOrderService.createHotelOrder(user, booking);
    }

    public PageResult<HotelBookingVO> enterpriseBookings(String status, Long eventId, Integer page, Integer size) {
        enterpriseContext.requireTenantId();
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        LambdaQueryWrapper<HotelBooking> wrapper = new LambdaQueryWrapper<HotelBooking>()
                .orderByDesc(HotelBooking::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(HotelBooking::getStatus, status);
        }
        if (eventId != null) {
            wrapper.eq(HotelBooking::getEventId, eventId);
        }
        Page<HotelBooking> result = hotelBookingMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        Map<Long, EvtEvent> eventMap = loadEvents(result.getRecords());
        Map<Long, SysUser> userMap = loadUsers(result.getRecords());
        Map<Long, HotelRoomType> roomMap = loadRoomTypes(result.getRecords());
        Map<Long, HotelInfo> hotelMap = loadHotels(roomMap.values());
        List<HotelBookingVO> records = result.getRecords().stream()
                .map(b -> toBookingVO(b, eventMap, userMap, roomMap, hotelMap))
                .toList();
        return new PageResult<>(records, result.getTotal());
    }

    @Transactional
    public void verifyBooking(Long id) {
        enterpriseContext.requireTenantId();
        HotelBooking booking = hotelBookingMapper.selectById(id);
        if (booking == null) {
            throw new BizException(404, "房单不存在");
        }
        if (!"LOCKED".equals(booking.getStatus())) {
            throw new BizException("仅已支付锁定的房单可核销");
        }
        booking.setStatus("CHECKED_IN");
        booking.setCheckedInAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        hotelBookingMapper.updateById(booking);
    }

    @Transactional
    public void onPaySuccess(PayOrder order) {
        if (!"HOTEL".equals(order.getBizType())) {
            return;
        }
        HotelBooking booking = hotelBookingMapper.selectById(order.getBizId());
        if (booking == null || !"PENDING_PAY".equals(booking.getStatus())) {
            return;
        }
        HotelQuota quota = requireQuota(booking.getEventId(), booking.getRoomTypeId());
        int updated = hotelQuotaMapper.update(null, new LambdaUpdateWrapper<HotelQuota>()
                .eq(HotelQuota::getId, quota.getId())
                .apply("used < total")
                .setSql("used = used + 1"));
        if (updated == 0) {
            throw new BizException("该房型配额已满，无法完成支付");
        }
        booking.setStatus("LOCKED");
        booking.setUpdatedAt(LocalDateTime.now());
        hotelBookingMapper.updateById(booking);
    }

    @Transactional
    public void onOrderClosed(PayOrder order) {
        if (!"HOTEL".equals(order.getBizType())) {
            return;
        }
        HotelBooking booking = hotelBookingMapper.selectById(order.getBizId());
        if (booking == null || !"PENDING_PAY".equals(booking.getStatus())) {
            return;
        }
        booking.setStatus("CANCELLED");
        booking.setUpdatedAt(LocalDateTime.now());
        hotelBookingMapper.updateById(booking);
    }

    private void deleteRoomTypeInternal(Long hotelId, Long id) {
        HotelRoomType roomType = requireRoomType(hotelId, id);
        hotelQuotaMapper.delete(new LambdaQueryWrapper<HotelQuota>()
                .eq(HotelQuota::getRoomTypeId, roomType.getId()));
        hotelRoomTypeMapper.deleteById(id);
    }

    private void syncQuota(Long tenantId, Long roomTypeId, int total) {
        List<EvtEvent> events = listHotelEnabledEvents(tenantId);
        if (events.isEmpty()) {
            throw new BizException("请先创建并开启酒店功能的活动");
        }
        for (EvtEvent event : events) {
            HotelQuota quota = new HotelQuota();
            quota.setTenantId(tenantId);
            quota.setEventId(event.getId());
            quota.setRoomTypeId(roomTypeId);
            quota.setTotal(total);
            quota.setUsed(0);
            hotelQuotaMapper.insert(quota);
        }
    }

    private void updateQuotaTotal(Long tenantId, Long roomTypeId, int total) {
        List<HotelQuota> quotas = hotelQuotaMapper.selectList(new LambdaQueryWrapper<HotelQuota>()
                .eq(HotelQuota::getRoomTypeId, roomTypeId));
        for (HotelQuota quota : quotas) {
            if (total < (quota.getUsed() == null ? 0 : quota.getUsed())) {
                throw new BizException("配额不能小于已用数量");
            }
            quota.setTotal(total);
            hotelQuotaMapper.updateById(quota);
        }
        if (quotas.isEmpty()) {
            syncQuota(tenantId, roomTypeId, total);
        }
    }

    private HotelQuota requireQuota(Long eventId, Long roomTypeId) {
        HotelQuota quota = hotelQuotaMapper.selectOne(new LambdaQueryWrapper<HotelQuota>()
                .eq(HotelQuota::getEventId, eventId)
                .eq(HotelQuota::getRoomTypeId, roomTypeId)
                .last("LIMIT 1"));
        if (quota == null) {
            throw new BizException("该活动未配置此房型配额");
        }
        return quota;
    }

    private HotelQuota lockQuota(Long eventId, Long roomTypeId) {
        Long tenantId = portalContext.requireTenantId();
        HotelQuota quota = hotelQuotaMapper.selectForUpdate(eventId, roomTypeId, tenantId);
        if (quota == null) {
            throw new BizException("该活动未配置此房型配额");
        }
        return quota;
    }

    private long countActiveBookings(Long eventId, Long roomTypeId) {
        return hotelBookingMapper.selectCount(new LambdaQueryWrapper<HotelBooking>()
                .eq(HotelBooking::getEventId, eventId)
                .eq(HotelBooking::getRoomTypeId, roomTypeId)
                .in(HotelBooking::getStatus, ACTIVE_BOOKING_STATUSES));
    }

    private List<EvtEvent> listHotelEnabledEvents(Long tenantId) {
        return evtEventMapper.selectList(new LambdaQueryWrapper<EvtEvent>()
                .eq(EvtEvent::getTenantId, tenantId)
                .eq(EvtEvent::getHotelEnabled, 1)
                .in(EvtEvent::getStatus, PORTAL_EVENT_STATUSES));
    }

    private Long resolveDefaultEventId(Long tenantId) {
        List<EvtEvent> events = listHotelEnabledEvents(tenantId);
        if (!events.isEmpty()) {
            return events.getFirst().getId();
        }
        EvtEvent event = evtEventMapper.selectOne(new LambdaQueryWrapper<EvtEvent>()
                .eq(EvtEvent::getTenantId, tenantId)
                .orderByDesc(EvtEvent::getCreatedAt)
                .last("LIMIT 1"));
        return event == null ? 0L : event.getId();
    }

    private EvtEvent requireHotelEvent(Long eventId) {
        portalContext.requireTenantId();
        EvtEvent event = evtEventMapper.selectById(eventId);
        if (event == null || !intToBool(event.getHotelEnabled()) || !PORTAL_EVENT_STATUSES.contains(event.getStatus())) {
            throw new BizException("活动未开放酒店预订");
        }
        return event;
    }

    private HotelInfo requireHotel(Long id) {
        enterpriseContext.requireTenantId();
        HotelInfo hotel = hotelInfoMapper.selectById(id);
        if (hotel == null) {
            throw new BizException(404, "酒店不存在");
        }
        return hotel;
    }

    private HotelRoomType requireRoomType(Long hotelId, Long id) {
        HotelRoomType roomType = hotelRoomTypeMapper.selectById(id);
        if (roomType == null || !hotelId.equals(roomType.getHotelId())) {
            throw new BizException(404, "房型不存在");
        }
        return roomType;
    }

    private Map<Long, EvtEvent> loadEvents(List<HotelBooking> bookings) {
        List<Long> ids = bookings.stream().map(HotelBooking::getEventId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return evtEventMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(EvtEvent::getId, e -> e));
    }

    private Map<Long, SysUser> loadUsers(List<HotelBooking> bookings) {
        List<Long> ids = bookings.stream().map(HotelBooking::getUserId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    private Map<Long, HotelRoomType> loadRoomTypes(List<HotelBooking> bookings) {
        List<Long> ids = bookings.stream().map(HotelBooking::getRoomTypeId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return hotelRoomTypeMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(HotelRoomType::getId, r -> r));
    }

    private Map<Long, HotelInfo> loadHotels(java.util.Collection<HotelRoomType> roomTypes) {
        List<Long> ids = roomTypes.stream().map(HotelRoomType::getHotelId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return hotelInfoMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(HotelInfo::getId, h -> h));
    }

    private HotelInfoVO toHotelVO(HotelInfo hotel) {
        HotelInfoVO vo = new HotelInfoVO();
        BeanUtils.copyProperties(hotel, vo);
        return vo;
    }

    private HotelRoomTypeVO toRoomTypeVO(HotelRoomType roomType, Long eventId) {
        HotelRoomTypeVO vo = new HotelRoomTypeVO();
        BeanUtils.copyProperties(roomType, vo);
        HotelQuota quota = hotelQuotaMapper.selectOne(new LambdaQueryWrapper<HotelQuota>()
                .eq(HotelQuota::getRoomTypeId, roomType.getId())
                .eq(HotelQuota::getEventId, eventId)
                .last("LIMIT 1"));
        if (quota == null) {
            quota = hotelQuotaMapper.selectOne(new LambdaQueryWrapper<HotelQuota>()
                    .eq(HotelQuota::getRoomTypeId, roomType.getId())
                    .last("LIMIT 1"));
        }
        if (quota != null) {
            vo.setQuota(quota.getTotal());
            vo.setUsed(quota.getUsed() == null ? 0 : quota.getUsed());
        } else {
            vo.setQuota(0);
            vo.setUsed(0);
        }
        return vo;
    }

    private HotelBookingVO toBookingVO(HotelBooking booking, Map<Long, EvtEvent> eventMap,
                                     Map<Long, SysUser> userMap, Map<Long, HotelRoomType> roomMap,
                                     Map<Long, HotelInfo> hotelMap) {
        HotelBookingVO vo = new HotelBookingVO();
        BeanUtils.copyProperties(booking, vo);
        EvtEvent event = eventMap.get(booking.getEventId());
        if (event != null) {
            vo.setEventTitle(event.getTitle());
        }
        SysUser user = userMap.get(booking.getUserId());
        if (user != null) {
            vo.setGuestName(user.getNickname());
        }
        HotelRoomType roomType = roomMap.get(booking.getRoomTypeId());
        if (roomType != null) {
            vo.setRoomTypeName(roomType.getName());
            HotelInfo hotel = hotelMap.get(roomType.getHotelId());
            if (hotel != null) {
                vo.setHotelName(hotel.getName());
            }
        }
        return vo;
    }

    private String genBookingNo() {
        return "B" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) (Math.random() * 900 + 100);
    }

    private boolean intToBool(Integer value) {
        return value != null && value == 1;
    }
}
