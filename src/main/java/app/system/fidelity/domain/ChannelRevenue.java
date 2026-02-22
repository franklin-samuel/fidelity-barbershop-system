package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.ReferralSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRevenue {
    private ReferralSource channel;
    private BigDecimal averageTicket;
    private Long customerCount;
}
