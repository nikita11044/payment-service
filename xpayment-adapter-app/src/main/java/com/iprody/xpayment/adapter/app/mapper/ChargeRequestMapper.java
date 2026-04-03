package com.iprody.xpayment.adapter.app.mapper;

import com.iprody.xpayment.adapter.app.dto.ChargeRequestDto;
import com.iprody.xpayment.app.api.model.CreateChargeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeRequestMapper {

    CreateChargeRequest toEntity(ChargeRequestDto chargeRequestDto);

    ChargeRequestDto toDto(CreateChargeRequest chargeRequest);
}
