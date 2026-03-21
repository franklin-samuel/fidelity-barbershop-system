package app.system.fidelity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCashClosing {

    private LocalDate date;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalRevenue;
    private Long totalAppointments;
    private List<PaymentMethodSummary> byPaymentMethod;

}