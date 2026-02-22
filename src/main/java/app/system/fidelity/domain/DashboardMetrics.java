package app.system.fidelity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {

    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;

    private List<DailyRevenue> last30DaysRevenue;

    private BigDecimal currentMonthRevenue;
    private BigDecimal previousMonthRevenue;
    private BigDecimal monthlyGrowthPercentage;

    private List<BarberPerformance> topBarbers;

    private BigDecimal servicesRevenue;
    private BigDecimal productsRevenue;

    private BigDecimal todayEarnings;
    private BigDecimal weekEarnings;
    private BigDecimal monthEarnings;
    private List<DailyRevenue> last30DaysEarnings;

    private Integer monthAppointments;
    private BigDecimal monthAverageTicket;
    private BigDecimal monthTotalTips;

    private BigDecimal monthCommission;
    private BigDecimal monthTips;

}
