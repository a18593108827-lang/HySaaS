package com.hysaas.hotel.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysaas.hotel.entity.HotelQuota;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HotelQuotaMapper extends BaseMapper<HotelQuota> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT id, tenant_id, event_id, room_type_id, total, used
            FROM hotel_quota
            WHERE event_id = #{eventId} AND room_type_id = #{roomTypeId} AND tenant_id = #{tenantId}
            LIMIT 1 FOR UPDATE
            """)
    HotelQuota selectForUpdate(@Param("eventId") Long eventId, @Param("roomTypeId") Long roomTypeId, @Param("tenantId") Long tenantId);
}
