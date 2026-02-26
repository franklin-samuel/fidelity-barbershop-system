package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record AnalyticsDataResponse(

        @JsonProperty("total_customers")
        Long totalCustomers,

        @JsonProperty("average_ticket")
        BigDecimal averageTicket,

        @JsonProperty("total_revenue")
        BigDecimal totalRevenue,

        @JsonProperty("retention_rate")
        Double retentionRate,

        @JsonProperty("new_customers_count")
        Long newCustomersCount,

        @JsonProperty("new_customers_count_previous_period")
        Long newCustomersCountPreviousPeriod,

        @JsonProperty("new_customers_growth_absolute")
        Long newCustomersGrowthAbsolute,

        @JsonProperty("average_ticket_current")
        BigDecimal averageTicketCurrent,

        @JsonProperty("average_ticket_previous")
        BigDecimal averageTicketPrevious,

        @JsonProperty("average_ticket_growth_absolute")
        BigDecimal averageTicketGrowthAbsolute,

        @JsonProperty("average_ticket_growth_percentage")
        BigDecimal averageTicketGrowthPercentage,

        @JsonProperty("revenue_by_weekday")
        Map<String, BigDecimal> revenueByWeekday,

        @JsonProperty("customers_by_age_group")
        Map<String, Long> customersByAgeGroup,

        @JsonProperty("customers_by_gender")
        Map<String, Long> customersByGender,

        @JsonProperty("acquisition_channels")
        List<ChannelDataResponse> acquisitionChannels,

        @JsonProperty("popular_styles")
        List<StyleDataResponse> popularStyles,

        @JsonProperty("preferred_frequency")
        Map<String, Long> preferredFrequency,

        @JsonProperty("top_customers")
        List<TopCustomerResponse> topCustomers,

        @JsonProperty("avg_ticket_by_age")
        Map<String, BigDecimal> avgTicketByAge,

        @JsonProperty("channel_vs_revenue")
        List<ChannelRevenueResponse> channelVsRevenue

) {

    @Builder
    public record ChannelDataResponse(
            String channel,

            @JsonProperty("customer_count")
            Long customerCount,

            Double percentage
    ) {}

    @Builder
    public record StyleDataResponse(
            String style,
            Long count,
            Double percentage
    ) {}

    @Builder
    public record TopCustomerResponse(
            String name,

            @JsonProperty("total_spent")
            BigDecimal totalSpent,

            @JsonProperty("visits_count")
            Integer visitsCount
    ) {}

    @Builder
    public record ChannelRevenueResponse(
            String channel,

            @JsonProperty("average_ticket")
            BigDecimal averageTicket,

            @JsonProperty("customer_count")
            Long customerCount
    ) {}
}