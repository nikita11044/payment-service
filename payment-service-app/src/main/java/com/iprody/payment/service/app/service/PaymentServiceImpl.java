package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.exception.EntityNotFoundException;
import com.iprody.payment.service.app.exception.Operation;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistency.PaymentFilterFactory;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto create(PaymentDto dto) {
        final Payment entity = paymentMapper.toEntity(dto);
        final Payment saved = paymentRepository.save(entity);
        return paymentMapper.toDto(saved);
    }

    @Override
    public PaymentDto get(UUID id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found for given id",
                        Operation.UPDATE_OP,
                        id
                ));

    }

    @Override
    public Page<PaymentDto> search(PaymentFilter filter, Pageable pageable) {
        final Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);
        return paymentRepository.findAll(spec, pageable).map(paymentMapper::toDto);
    }

    @Override
    public PaymentDto update(UUID id, PaymentDto dto) {
        if (!paymentRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Payment not found for given id",
                    Operation.UPDATE_OP,
                    id
            );
        }

        final Payment updated = paymentMapper.toEntity(dto);
        updated.setGuid(id);

        final Payment saved = paymentRepository.save(updated);
        return paymentMapper.toDto(saved);
    }

    @Override
    public PaymentDto updateStatus(UUID id, PaymentStatus status) {
        final Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException( "Payment not found for given id", Operation.UPDATE_OP, id));

        payment.setStatus(status);

        final Payment saved = paymentRepository.save(payment);
        return paymentMapper.toDto(saved);
    }

    @Override
    public PaymentDto updateNote(UUID id, String note) {
        final Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found for given id", Operation.UPDATE_OP, id));

        payment.setNote(note);

        final Payment saved = paymentRepository.save(payment);
        return paymentMapper.toDto(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!paymentRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Payment not found for given id",
                    Operation.UPDATE_OP,
                    id
            );
        }

        paymentRepository.deleteById(id);
    }
}

