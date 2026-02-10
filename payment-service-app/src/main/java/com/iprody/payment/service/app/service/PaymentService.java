package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Payment getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}

