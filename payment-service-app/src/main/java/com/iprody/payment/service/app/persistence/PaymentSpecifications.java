package com.iprody.payment.service.app.persistence;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentSpecifications {

    public static Specification<Payment> hasCurrency(String currency) {
        return (root, query, cb) -> cb.equal(root.get("currency"), currency);
    }

    public static Specification<Payment> amountGreater(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Payment> amountLess(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    public static Specification<Payment> createdGreater(Instant after) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), after);
    }

    public static Specification<Payment> createdLess(Instant before) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), before);
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
