package app.system.fidelity.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Customer extends AbstractDomain{

    private String name;
    private String email;
    private String phoneNumber;
    private Integer haircutCount;

}
