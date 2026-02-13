package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistency.PaymentFilterFactory;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
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

    public List<Payment> search(PaymentFilter filter) {
        final Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);
        return paymentRepository.findAll(spec);
    }

    public Page<Payment> searchPaged(PaymentFilter filter, Pageable pageable) {
        final Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);
        return paymentRepository.findAll(spec, pageable);
    }
}

