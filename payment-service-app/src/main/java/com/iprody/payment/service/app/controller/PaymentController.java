package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.payment.Payment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final Map<Long, Payment> payments = new HashMap<>();

    public PaymentController() {
        payments.put(1L, new Payment(1L, 99.99));
        payments.put(2L, new Payment(2L, 149.50));
        payments.put(3L, new Payment(3L, 250.00));
        payments.put(4L, new Payment(4L, 19.99));
        // Compilation  error to test actions
        payments.put();
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return payments.get(id);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments.values());
    }
}
