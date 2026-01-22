package app.system.fidelity.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Settings extends AbstractDomain{
    private Integer haircutsForFree;
}
