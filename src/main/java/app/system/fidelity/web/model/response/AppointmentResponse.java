package app.system.fidelity.web.model.response;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AppointmentResponse(

        UUID id,

        @JsonProperty("barber_id")
        UUID barberId,

        @JsonProperty("customer_id")
        UUID customerId,

        AppointmentType type,

        @JsonProperty("service_id")
        UUID serviceId,

        @JsonProperty("product_id")
        UUID productId,

        @JsonProperty("payment_method")
        PaymentMethod paymentMethod,

        BigDecimal tip,

        BigDecimal price,

        @JsonProperty("commission_percentage")
        BigDecimal commissionPercentage,

        @JsonProperty("commission_amount")
        BigDecimal commissionAmount,

        @JsonProperty("discount_amount")
        BigDecimal discountAmount,

        @JsonProperty("total_amount")
        BigDecimal totalAmount,

        @JsonProperty("loyalty_discount_applied")
        Boolean loyaltyDiscountApplied,

        @JsonProperty("created_at")
        LocalDateTime createdAt

) {}