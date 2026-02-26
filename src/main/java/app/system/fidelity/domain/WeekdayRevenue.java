package app.system.fidelity.domain;

import java.math.BigDecimal;

public record WeekdayRevenue(
        Integer dayOfWeek,
        BigDecimal totalRevenue,
        Long appointmentCount
) {}