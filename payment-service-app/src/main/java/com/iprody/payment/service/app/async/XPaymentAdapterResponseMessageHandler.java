package com.iprody.payment.service.app.async;

import com.iprody.payment.service.app.exception.EntityNotFoundException;
import com.iprody.payment.service.app.exception.Operation;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class XPaymentAdapterResponseMessageHandler implements MessageHandler<XPaymentAdapterResponseMessage> {

    private final PaymentRepository paymentRepository;

    @Override
    public void handle(XPaymentAdapterResponseMessage message) {
        final Payment payment = paymentRepository.findById(message.getPaymentGuid())
            .orElseThrow(() -> new EntityNotFoundException("Payment not found for given id",
            Operation.UPDATE_OP, message.getPaymentGuid()));

        payment.setAmount(message.getAmount());
        payment.setCurrency(message.getCurrency());
        payment.setTransactionRefId(message.getTransactionRefId());
        payment.setStatus(mapStatus(message.getStatus()));

        paymentRepository.save(payment);
    }

    private PaymentStatus mapStatus(XPaymentAdapterStatus adapterStatus) {
        return switch (adapterStatus) {
            case SUCCEEDED -> PaymentStatus.APPROVED;
            case CANCELED -> PaymentStatus.DECLINED;
            case PROCESSING -> PaymentStatus.PENDING;
        };
    }
}
