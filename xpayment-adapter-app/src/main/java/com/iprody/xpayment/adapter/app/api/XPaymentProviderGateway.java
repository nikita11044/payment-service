package com.iprody.xpayment.adapter.app.api;

import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import com.iprody.xpayment.adapter.app.dto.ChargeRequestDto;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

public interface XPaymentProviderGateway {
    ChargeResponseDto createCharge(ChargeRequestDto createChargeRequest) throws RestClientException;
    ChargeResponseDto retrieveCharge(UUID id) throws RestClientException;
}
