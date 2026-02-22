package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.AnalyticsData;
import app.system.fidelity.web.model.response.AnalyticsDataResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AnalyticsMapper {

    public AnalyticsDataResponse mapToResponse(final AnalyticsData data) {
        return AnalyticsDataResponse.builder()
                .totalCustomers(data.getTotalCustomers())
                .averageTicket(data.getAverageTicket())
                .totalRevenue(data.getTotalRevenue())
                .retentionRate(data.getRetentionRate())

                .customersByAgeGroup(data.getCustomersByAgeGroup())
                .topNeighborhoods(data.getTopNeighborhoods() != null
                        ? data.getTopNeighborhoods().stream()
                        .map(n -> AnalyticsDataResponse.NeighborhoodDataResponse.builder()
                                .neighborhood(n.getNeighborhood())
                                .customerCount(n.getCustomerCount())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .customersByGender(data.getCustomerByGender())

                .acquisitionChannels(data.getAcquisitionChannels() != null
                        ? data.getAcquisitionChannels().stream()
                        .map(c -> AnalyticsDataResponse.ChannelDataResponse.builder()
                                .channel(String.valueOf(c.getChannel()))
                                .customerCount(c.getCustomerCount())
                                .percentage(c.getPercentage())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .popularStyles(data.getPopularStyles() != null
                        ? data.getPopularStyles().stream()
                        .map(s -> AnalyticsDataResponse.StyleDataResponse.builder()
                                .style(String.valueOf(s.getStyle()))
                                .count(s.getCount())
                                .percentage(s.getPercentage())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .preferredFrequency(data.getPreferredFrequency())

                .topCustomers(data.getTopCustomers() != null
                        ? data.getTopCustomers().stream()
                        .map(t -> AnalyticsDataResponse.TopCustomerResponse.builder()
                                .name(t.getName())
                                .totalSpent(t.getTotalSpent())
                                .visitsCount(t.getVisitsCount())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .avgTicketByAge(data.getAvgTicketByAge())
                .channelVsRevenue(data.getChannelVsRevenue() != null
                        ? data.getChannelVsRevenue().stream()
                        .map(c -> AnalyticsDataResponse.ChannelRevenueResponse.builder()
                                .channel(String.valueOf(c.getChannel()))
                                .averageTicket(c.getAverageTicket())
                                .customerCount(c.getCustomerCount())
                                .build())
                        .collect(Collectors.toList())
                        : null)

                .build();
    }
}