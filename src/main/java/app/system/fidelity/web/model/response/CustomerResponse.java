package app.system.fidelity.web.model.response;

import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import app.system.fidelity.domain.enums.ReferralSource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        Gender gender,

        @JsonProperty("referral_source")
        ReferralSource referralSource,

        @JsonProperty("preferred_frequency")
        PreferredFrequency preferredFrequency,

        @JsonProperty("preferred_style")
        PreferredStyle preferredStyle,

        @JsonProperty("preferred_barber_id")
        UUID preferredBarberId,

        @JsonProperty("instagram_username")
        String instagramUsername,

        String occupation,

        @JsonProperty("last_visit_date")
        LocalDateTime lastVisitDate,

        @JsonProperty("total_spent")
        BigDecimal totalSpent,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("modified_at")
        LocalDateTime modifiedAt
) {}