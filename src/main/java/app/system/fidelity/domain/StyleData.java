package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.PreferredStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleData {
    private PreferredStyle style;
    private Long count;
    private Double percentage;
}
