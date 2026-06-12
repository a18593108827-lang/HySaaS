package com.hysaas.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysaas.payment.entity.PayTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayTransactionMapper extends BaseMapper<PayTransaction> {
}
