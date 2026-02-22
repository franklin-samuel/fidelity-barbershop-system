package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.AnalyticsData;
import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.web.model.response.AnalyticsDataResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
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

                .customersByGender(convertGenderMapToString(data.getCustomerByGender()))

                .acquisitionChannels(data.getAcquisitionChannels() != null
                        ? data.getAcquisitionChannels().stream()
                        .map(c -> AnalyticsDataResponse.ChannelDataResponse.builder()
                                .channel(c.getChannel() != null ? c.getChannel().name() : "NOT_INFORMED")
                                .customerCount(c.getCustomerCount())
                                .percentage(c.getPercentage())
                                .build())
                        .collect(Collectors.toList())
                        : null)

                .popularStyles(data.getPopularStyles() != null
                        ? data.getPopularStyles().stream()
                        .map(s -> AnalyticsDataResponse.StyleDataResponse.builder()
                                .style(s.getStyle() != null ? s.getStyle().name() : "NOT_INFORMED")
                                .count(s.getCount())
                                .percentage(s.getPercentage())
                                .build())
                        .collect(Collectors.toList())
                        : null)

                .preferredFrequency(convertPreferredFrequencyMapToString(data.getPreferredFrequency()))

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
                                .channel(c.getChannel() != null ? c.getChannel().name() : "NOT_INFORMED")
                                .averageTicket(c.getAverageTicket())
                                .customerCount(c.getCustomerCount())
                                .build())
                        .collect(Collectors.toList())
                        : null)

                .build();
    }

    private Map<String, Long> convertGenderMapToString(final Map<Gender, Long> genderMap) {
        if (genderMap == null) {
            return null;
        }

        final Map<String, Long> result = new LinkedHashMap<>();
        genderMap.forEach((gender, count) -> {
            final String key = gender != null ? gender.name() : "NOT_INFORMED";
            result.put(key, count);
        });

        return result;
    }

    private Map<String, Long> convertPreferredFrequencyMapToString(final Map<PreferredFrequency, Long> frequencyMap) {
        if (frequencyMap == null) {
            return null;
        }

        final Map<String, Long> result = new LinkedHashMap<>();
        frequencyMap.forEach((frequency, count) -> {
            final String key = frequency != null ? frequency.name() : "OT_INFORMED";
            result.put(key, count);
        });

        return result;
    }
}