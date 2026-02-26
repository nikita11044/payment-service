package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PaymentService {

    PaymentDto create(PaymentDto dto);

    PaymentDto get(UUID id);

    Page<PaymentDto> search(PaymentFilter filter, Pageable pageable);

    PaymentDto update(UUID id, PaymentDto dto);

    PaymentDto updateStatus(UUID id, PaymentStatus status);

    PaymentDto updateNote(UUID id, String note);

    void delete(UUID id);
}
