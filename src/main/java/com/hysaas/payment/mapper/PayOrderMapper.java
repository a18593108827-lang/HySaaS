package com.hysaas.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysaas.payment.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
