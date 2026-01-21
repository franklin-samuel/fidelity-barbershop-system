package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CustomerResponse(
        UUID id,
        String name,
        String email,

        @JsonProperty("phone_number")
        String phoneNumber,

        @JsonProperty("haircut_count")
        Integer haircutCount,

        @JsonProperty("free_haircuts_claimed")
        Integer freeHaircutsClaimed,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}
