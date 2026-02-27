package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.dto.PaymentNoteUpdateDto;
import com.iprody.payment.service.app.dto.PaymentStatusUpdateDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PaymentDto getPayment(@PathVariable UUID id) {
        return paymentService.get(id);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public Page<PaymentDto> searchPayments(
        @ModelAttribute PaymentFilter filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(defaultValue = "updatedAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        final Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        final Pageable pageable = PageRequest.of(page, size, sort);
        return paymentService.search(filter, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto createPayment(@RequestBody PaymentDto dto) {
        return paymentService.create(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PaymentDto updatePayment(@PathVariable UUID id, @RequestBody PaymentDto dto) {
        return paymentService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public PaymentDto updateStatus(
        @PathVariable UUID id,
        @RequestBody @Valid PaymentStatusUpdateDto dto
    ) {
        return paymentService.updateStatus(id, dto.getStatus());
    }

    @PatchMapping("/{id}/note")
    public PaymentDto updateStatus(
        @PathVariable UUID id,
        @RequestBody @Valid PaymentNoteUpdateDto dto
    ) {
        return paymentService.updateNote(id, dto.getNote());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(@PathVariable UUID id) {
        paymentService.delete(id);
    }
}
