package com.iprody.xpayment.adapter.app.checkstate.handler;

import com.iprody.xpayment.adapter.app.api.XPaymentProviderGateway;
import com.iprody.xpayment.adapter.app.async.XPaymentAdapterStatus;
import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentStatusCheckHandlerImpl implements PaymentStatusCheckHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentStatusCheckHandlerImpl.class);

    private final XPaymentProviderGateway xPaymentProviderGateway;

    @Override
    public boolean handle(UUID paymentGuid) {
        final ChargeResponseDto charge = xPaymentProviderGateway.retrieveCharge(paymentGuid);
        final XPaymentAdapterStatus status = XPaymentAdapterStatus.valueOf(charge.status());

        if (status == XPaymentAdapterStatus.PROCESSING) {
            log.debug("Charge {} is still processing", paymentGuid);
            return false;
        }

        log.info("Charge {} reached terminal status: {}", paymentGuid, status);
        return true;
    }
}
