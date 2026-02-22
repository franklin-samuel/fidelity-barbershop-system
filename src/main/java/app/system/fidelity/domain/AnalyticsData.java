package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsData {

    private Long totalCustomers;
    private BigDecimal averageTicket;
    private BigDecimal totalRevenue;
    private Double retentionRate;

    private Map<String, Long> customersByAgeGroup;
    private List<NeighborhoodData> topNeighborhoods;
    private Map<Gender, Long> customerByGender;

    private List<ChannelData> acquisitionChannels;
    private List<StyleData> popularStyles;
    private Map<PreferredFrequency, Long> preferredFrequency;

    private List<TopCustomer> topCustomers;
    private Map<String, BigDecimal> avgTicketByAge;
    private List<ChannelRevenue> channelVsRevenue;

}
