package com.iprody.xpayment.adapter.app.api;

import com.iprody.xpayment.adapter.app.dto.ChargeRequestDto;
import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import com.iprody.xpayment.adapter.app.mapper.ChargeRequestMapper;
import com.iprody.xpayment.adapter.app.mapper.ChargeResponseMapper;

import com.iprody.xpayment.app.api.client.DefaultApi;
import com.iprody.xpayment.app.api.model.ChargeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class XPaymentProviderGatewayImpl implements XPaymentProviderGateway {

    private final DefaultApi defaultApi;

    private final ChargeRequestMapper chargeRequestMapper;

    private final ChargeResponseMapper chargeResponseMapper;

    @Override
    public ChargeResponseDto createCharge(ChargeRequestDto chargeRequestDto) {
        try {
            final ChargeResponse response = defaultApi.createCharge(
                chargeRequestMapper.toEntity(chargeRequestDto));
            return chargeResponseMapper.toDto(response);
        } catch (HttpClientErrorException e) {
            throw new RestClientException(
                    String.format("POST /charges failed: HTTP %s, body: %s",
                            e.getStatusCode(), e.getResponseBodyAsString()), e);
        }
    }

    @Override
    public ChargeResponseDto retrieveCharge(UUID id) {
        try {
            final ChargeResponse response = defaultApi.retrieveCharge(id);
            return chargeResponseMapper.toDto(response);
        } catch (HttpClientErrorException e) {
            throw new RestClientException(
                    String.format("GET /charges/%s failed: HTTP %s, body: %s",
                            id, e.getStatusCode(), e.getResponseBodyAsString()), e);
        }
    }
}
