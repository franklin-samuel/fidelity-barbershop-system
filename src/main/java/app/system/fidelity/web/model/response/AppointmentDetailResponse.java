package app.system.fidelity.web.model.response;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppointmentDetailResponse(

        UUID id,

        @JsonProperty("barber_id")
        UUID barberId,

        @JsonProperty("barber_name")
        String barberName,

        @JsonProperty("customer_id")
        UUID customerId,

        @JsonProperty("customer_name")
        String customerName,

        AppointmentType type,

        @JsonProperty("service_id")
        UUID serviceId,

        @JsonProperty("service_name")
        String serviceName,

        @JsonProperty("product_id")
        UUID productId,

        @JsonProperty("product_name")
        String productName,

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

        @JsonProperty("barber_total")
        BigDecimal barberTotal,

        @JsonProperty("barbershop_revenue")
        BigDecimal barbershopRevenue,

        @JsonProperty("created_at")
        LocalDateTime createdAt

) {}