package app.system.fidelity.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenRequest(
        @JsonProperty(value = "refresh_token")
        String refreshToken
) {
}
