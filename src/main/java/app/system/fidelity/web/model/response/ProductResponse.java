package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProductResponse(

        UUID id,

        String name,

        BigDecimal price,

        @JsonProperty("commission_percentage")
        BigDecimal commissionPercentage,

        Boolean active,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("modified_at")
        LocalDateTime modifiedAt

) {}