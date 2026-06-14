package com.hysaas.event.dto;

import com.hysaas.payment.dto.PayOrderVO;
import lombok.Data;

@Data
public class PortalRegisterResultVO {

    private RegistrationVO registration;
    private PayOrderVO payOrder;
}
