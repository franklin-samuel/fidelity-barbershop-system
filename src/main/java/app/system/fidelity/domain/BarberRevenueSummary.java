package app.system.fidelity.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record BarberRevenueSummary(
        UUID barberId,
        BigDecimal revenue,
        Long appointmentsCount
) {}