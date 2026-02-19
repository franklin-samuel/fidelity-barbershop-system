package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.Role;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractDomain {

    private String name;
    private String email;
    private String password;
    private Role role;

}
