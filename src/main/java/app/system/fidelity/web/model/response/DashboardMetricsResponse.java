package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardMetricsResponse(

        @JsonProperty("today_revenue")
        BigDecimal todayRevenue,

        @JsonProperty("week_revenue")
        BigDecimal weekRevenue,

        @JsonProperty("month_revenue")
        BigDecimal monthRevenue,

        @JsonProperty("last_30_days_revenue")
        List<DailyRevenueResponse> last30DaysRevenue,

        @JsonProperty("current_month_revenue")
        BigDecimal currentMonthRevenue,

        @JsonProperty("previous_month_revenue")
        BigDecimal previousMonthRevenue,

        @JsonProperty("monthly_growth_percentage")
        BigDecimal monthlyGrowthPercentage,

        @JsonProperty("top_barbers")
        List<BarberPerformanceResponse> topBarbers,

        @JsonProperty("services_revenue")
        BigDecimal servicesRevenue,

        @JsonProperty("products_revenue")
        BigDecimal productsRevenue,

        @JsonProperty("today_earnings")
        BigDecimal todayEarnings,

        @JsonProperty("week_earnings")
        BigDecimal weekEarnings,

        @JsonProperty("month_earnings")
        BigDecimal monthEarnings,

        @JsonProperty("last_30_days_earnings")
        List<DailyRevenueResponse> last30DaysEarnings,

        @JsonProperty("month_appointments")
        Integer monthAppointments,

        @JsonProperty("month_average_ticket")
        BigDecimal monthAverageTicket,

        @JsonProperty("month_total_tips")
        BigDecimal monthTotalTips,

        @JsonProperty("month_commission")
        BigDecimal monthCommission,

        @JsonProperty("month_tips")
        BigDecimal monthTips

) {
    @Builder
    public record DailyRevenueResponse(
            LocalDate date,
            BigDecimal amount
    ) {}

    @Builder
    public record BarberPerformanceResponse(
            @JsonProperty("barber_name")
            String barberName,

            @JsonProperty("total_revenue")
            BigDecimal totalRevenue,

            @JsonProperty("appointments_count")
            Integer appointmentsCount
    ) {}
}