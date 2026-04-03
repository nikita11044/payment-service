package com.iprody.xpayment.adapter.app.async;

import com.iprody.xpayment.adapter.app.api.XPaymentProviderGateway;
import com.iprody.xpayment.adapter.app.dto.ChargeRequestDto;
import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private static final Logger logger = LoggerFactory.getLogger(RequestMessageHandler.class);

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final AsyncSender<XPaymentAdapterResponseMessage> asyncSender;

    @Override
    public void handle(XPaymentAdapterRequestMessage message) {
        logger.info(
            "Payment request received paymentGuid - {}, amount - {}, currency - {}",
            message.getPaymentId(),
            message.getAmount(),
            message.getCurrency()
        );

        final ChargeRequestDto dto = ChargeRequestDto.builder()
            .amount(message.getAmount())
            .currency(message.getCurrency())
            .order(message.getPaymentId())
            .build();

        try {
            final ChargeResponseDto chargeResponseDto =
                xPaymentProviderGateway.createCharge(dto);

            logger.info(
                "Payment request with paymentGuid - {} is sent for payment processing. Current status - {}",
                chargeResponseDto.order(),
                chargeResponseDto.status()
            );

            final XPaymentAdapterResponseMessage responseMessage = XPaymentAdapterResponseMessage.builder()
                .paymentGuid(chargeResponseDto.order())
                .transactionRefId(chargeResponseDto.id())
                .amount(chargeResponseDto.amount())
                .currency(chargeResponseDto.currency())
                .status(XPaymentAdapterStatus.valueOf(chargeResponseDto.status()))
                .occurredAt(Instant.now())
                .build();

            asyncSender.send(responseMessage);

        } catch (RestClientException ex) {
            logger.error(
                "Error in time of sending payment request with paymentGuid - {}",
                message.getPaymentId(), ex
            );

            final XPaymentAdapterResponseMessage responseMessage = XPaymentAdapterResponseMessage.builder()
                .paymentGuid(message.getPaymentId())
                .amount(message.getAmount())
                .currency(message.getCurrency())
                .status(XPaymentAdapterStatus.CANCELED)
                .occurredAt(Instant.now())
                .build();

            asyncSender.send(responseMessage);
        }
    }
}
