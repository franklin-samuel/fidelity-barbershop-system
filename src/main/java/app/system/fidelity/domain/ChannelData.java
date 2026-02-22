package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.ReferralSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelData {
    private ReferralSource channel;
    private Long customerCount;
    private Double percentage;
}
