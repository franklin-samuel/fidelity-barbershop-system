package app.system.fidelity.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@Builder
public record JwtResponse(
        @JsonProperty(value = "access_token")
        String accessToken,

        @JsonProperty(value = "refresh_token")
        String refreshToken,

        String type,
        List<String> role
) {

}
