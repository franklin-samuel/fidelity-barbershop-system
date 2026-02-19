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

        @JsonProperty("service_count")
        Integer serviceCount,

        @JsonProperty("discounts_claimed")
        Integer discountsClaimed,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("modified_at")
        LocalDateTime modifiedAt
) {}