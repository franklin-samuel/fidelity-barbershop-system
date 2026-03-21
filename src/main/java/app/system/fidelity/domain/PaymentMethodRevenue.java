package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentMethodRevenue(
        PaymentMethod paymentMethod,
        BigDecimal revenue,
        Long appointments
) {}