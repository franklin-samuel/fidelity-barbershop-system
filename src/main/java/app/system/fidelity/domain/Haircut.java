package app.system.fidelity.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Haircut extends AbstractDomain{

    private UUID customerId;
    private UUID registeredBy;
    private Boolean isFree;

}
