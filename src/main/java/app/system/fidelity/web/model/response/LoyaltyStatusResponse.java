package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LoyaltyStatusResponse(

        @JsonProperty("has_discount")
        boolean hasDiscount,

        @JsonProperty("service_count")
        Integer serviceCount,

        @JsonProperty("discounts_claimed")
        Integer discountsClaimed,

        @JsonProperty("original_price")
        BigDecimal originalPrice,

        @JsonProperty("discount_amount")
        BigDecimal discountAmount,

        @JsonProperty("total_amount")
        BigDecimal totalAmount

) {}