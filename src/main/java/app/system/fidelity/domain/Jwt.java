package app.system.fidelity.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Jwt extends AbstractDomain {
    private String accessToken;
    private String refreshToken;
    private String type;
}

