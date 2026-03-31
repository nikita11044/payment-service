package com.iprody.xpayment.adapter.app.mapper;

import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import com.iprody.xpayment.app.api.model.ChargeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeResponseMapper {

    ChargeResponse toEntity(ChargeResponseDto chargeResponseDto);

    ChargeResponseDto toDto(ChargeResponse chargeResponse);
}
