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
public class BarberPerformance {
    private String barberName;
    private BigDecimal totalRevenue;
    private Integer appointmentsCount;
}
