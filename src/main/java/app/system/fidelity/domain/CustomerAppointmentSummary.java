package app.system.fidelity.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerAppointmentSummary(
        UUID customerId,
        BigDecimal totalRevenue,
        Long appointmentCount
) {}