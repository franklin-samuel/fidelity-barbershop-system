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

    private UUID customer_id;
    private UUID registered_by;
    private Boolean is_free;

}
