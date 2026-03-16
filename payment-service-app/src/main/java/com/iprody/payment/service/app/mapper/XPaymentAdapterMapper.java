package com.iprody.payment.service.app.mapper;

import com.iprody.payment.service.app.async.XPaymentAdapterRequestMessage;
import com.iprody.payment.service.app.persistence.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface XPaymentAdapterMapper {

    @Mapping(source = "guid", target = "paymentId")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "updatedAt", target = "occurredAt")
    XPaymentAdapterRequestMessage toXPaymentAdapterRequestMessage(Payment payment);
}
