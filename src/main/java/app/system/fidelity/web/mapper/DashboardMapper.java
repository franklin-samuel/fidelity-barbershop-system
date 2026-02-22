package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.DashboardMetrics;
import app.system.fidelity.web.model.response.DashboardMetricsResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DashboardMapper {

    public DashboardMetricsResponse mapToResponse(final DashboardMetrics metrics, final boolean isAdmin) {
        if (isAdmin) {
            return DashboardMetricsResponse.builder()
                    .todayRevenue(metrics.getTodayRevenue())
                    .weekRevenue(metrics.getWeekRevenue())
                    .monthRevenue(metrics.getMonthRevenue())

                    .last30DaysRevenue(metrics.getLast30DaysRevenue() != null
                            ? metrics.getLast30DaysRevenue().stream()
                            .map(day -> DashboardMetricsResponse.DailyRevenueResponse.builder()
                                    .date(day.getDate())
                                    .amount(day.getAmount())
                                    .build())
                            .collect(Collectors.toList())
                            : null)

                    .currentMonthRevenue(metrics.getCurrentMonthRevenue())
                    .previousMonthRevenue(metrics.getPreviousMonthRevenue())
                    .monthlyGrowthPercentage(metrics.getMonthlyGrowthPercentage())

                    .topBarbers(metrics.getTopBarbers() != null
                            ? metrics.getTopBarbers().stream()
                            .map(barber -> DashboardMetricsResponse.BarberPerformanceResponse.builder()
                                    .barberName(barber.getBarberName())
                                    .totalRevenue(barber.getTotalRevenue())
                                    .appointmentsCount(barber.getAppointmentsCount())
                                    .build())
                            .collect(Collectors.toList())
                            : null)

                    .servicesRevenue(metrics.getServicesRevenue())
                    .productsRevenue(metrics.getProductsRevenue())

                    .build();
        } else {
            return DashboardMetricsResponse.builder()
                    .todayEarnings(metrics.getTodayEarnings())
                    .weekEarnings(metrics.getWeekEarnings())
                    .monthEarnings(metrics.getMonthEarnings())

                    .last30DaysEarnings(metrics.getLast30DaysEarnings() != null
                            ? metrics.getLast30DaysEarnings().stream()
                            .map(day -> DashboardMetricsResponse.DailyRevenueResponse.builder()
                                    .date(day.getDate())
                                    .amount(day.getAmount())
                                    .build())
                            .collect(Collectors.toList())
                            : null)

                    .monthAppointments(metrics.getMonthAppointments())
                    .monthAverageTicket(metrics.getMonthAverageTicket())
                    .monthTotalTips(metrics.getMonthTotalTips())
                    .monthCommission(metrics.getMonthCommission())
                    .monthTips(metrics.getMonthTips())

                    .build();
        }
    }
}