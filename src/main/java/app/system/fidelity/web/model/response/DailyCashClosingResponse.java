package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DailyCashClosingResponse(

        LocalDate date,

        PeriodResponse period,

        @JsonProperty("total_revenue")
        BigDecimal totalRevenue,

        @JsonProperty("total_appointments")
        Long totalAppointments,

        @JsonProperty("by_payment_method")
        List<PaymentMethodSummaryResponse> byPaymentMethod

) {

    @Builder
    public record PeriodResponse(
            LocalDateTime start,
            LocalDateTime end
    ) {}

    @Builder
    public record PaymentMethodSummaryResponse(
            @JsonProperty("payment_method")
            String paymentMethod,

            BigDecimal revenue,

            Long appointments
    ) {}
}