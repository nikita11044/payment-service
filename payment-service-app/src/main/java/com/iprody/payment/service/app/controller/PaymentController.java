package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.dto.PaymentNoteUpdateDto;
import com.iprody.payment.service.app.dto.PaymentStatusUpdateDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public PaymentDto getPayment(@PathVariable UUID id) {
        log.info("GET payment by id: {}", id);

        final PaymentDto dto = paymentService.get(id);

        log.debug("Sending response PaymentDto: {}", dto);
        return dto;
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public Page<PaymentDto> searchPayments(
        @ModelAttribute PaymentFilter filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(defaultValue = "updatedAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        log.info("SEARCH payments with filter: {}", filter);

        final Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        final Pageable pageable = PageRequest.of(page, size, sort);

        final Page<PaymentDto> result = paymentService.search(filter, pageable);

        log.debug("Sending response Page<PaymentDto>: {}", result);
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public PaymentDto createPayment(@RequestBody PaymentDto dto) {
        log.info("CREATE payment");

        final PaymentDto result = paymentService.create(dto);

        log.debug("Sending response PaymentDto: {}", result);
        return result;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('admin')")
    public PaymentDto updatePayment(@PathVariable UUID id, @RequestBody PaymentDto dto) {
        log.info("UPDATE payment by id: {}", id);

        final PaymentDto result = paymentService.update(id, dto);

        log.debug("Sending response PaymentDto: {}", result);
        return result;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto updateStatus(
        @PathVariable UUID id,
        @RequestBody @Valid PaymentStatusUpdateDto dto
    ) {
        log.info("UPDATE payment status by id: {}", id);

        final PaymentDto result = paymentService.updateStatus(id, dto.getStatus());

        log.debug("Sending response PaymentDto: {}", result);
        return result;
    }

    @PatchMapping("/{id}/note")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto updateNote(
        @PathVariable UUID id,
        @RequestBody @Valid PaymentNoteUpdateDto dto
    ) {
        log.info("UPDATE payment note by id: {}", id);

        final PaymentDto result = paymentService.updateNote(id, dto.getNote());

        log.debug("Sending response PaymentDto: {}", result);
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void deletePayment(@PathVariable UUID id) {
        log.info("DELETE payment by id: {}", id);

        paymentService.delete(id);

        log.debug("Payment deleted: {}", id);
    }
}
