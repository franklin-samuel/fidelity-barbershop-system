package app.system.fidelity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReport {

    private YearMonth reportMonth;
    private YearMonth previousMonth;

    private BigDecimal totalRevenue;
    private BigDecimal previousMonthRevenue;
    private BigDecimal revenueGrowthAbsolute;
    private BigDecimal revenueGrowthPercentage;

    private BigDecimal servicesRevenue;
    private BigDecimal productsRevenue;

    private Long totalAppointments;
    private Long previousMonthAppointments;

    private Long newCustomers;
    private Long previousMonthNewCustomers;

    private BigDecimal averageTicket;
    private BigDecimal previousMonthAverageTicket;

    private List<BarberMonthlyPerformance> barbersPerformance;

    private List<String> weakDaysInsights;

}