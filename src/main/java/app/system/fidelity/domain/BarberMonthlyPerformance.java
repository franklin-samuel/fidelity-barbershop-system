package app.system.fidelity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberMonthlyPerformance {
    private String barberName;
    private BigDecimal totalRevenue;
    private Long appointmentsCount;
    private BigDecimal averageTicket;
}
