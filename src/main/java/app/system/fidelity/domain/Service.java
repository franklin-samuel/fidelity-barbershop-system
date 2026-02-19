package app.system.fidelity.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Service extends AbstractDomain {

    private String name;
    private BigDecimal price;
    private BigDecimal commissionPercentage;

}
