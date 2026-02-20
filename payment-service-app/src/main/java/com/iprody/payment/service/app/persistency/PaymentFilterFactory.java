package com.iprody.payment.service.app.persistency;

import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.PaymentSpecifications;
import com.iprody.payment.service.app.persistence.entity.Payment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PaymentFilterFactory {

    public static Specification<Payment> fromFilter(PaymentFilter filter) {
        Specification<Payment> spec = Specification.unrestricted();

        if (StringUtils.hasText(filter.currency())) {
            spec = spec.and(PaymentSpecifications.hasCurrency(filter.currency()));
        }

        if (filter.minAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountGreater(
                    filter.minAmount()));
        }

        if (filter.maxAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountLess(
                    filter.maxAmount()));
        }

        if (filter.createdAfter() != null) {
            spec = spec.and(PaymentSpecifications.createdGreater(
                    filter.createdAfter()));
        }

        if (filter.createdBefore() != null) {
            spec = spec.and(PaymentSpecifications.createdLess(
                    filter.createdBefore()));
        }

        if (filter.status() != null) {
            spec = spec.and(PaymentSpecifications.hasStatus(filter.status()));
        }

        return spec;
    }
}
