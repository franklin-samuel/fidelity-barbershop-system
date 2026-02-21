package app.system.fidelity.web.model.request;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentRequest {

    @NotNull(message = "Meio de pagamento é obrigatório")
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("service_id")
    private UUID serviceId;

    @JsonProperty("product_id")
    private UUID productId;

    private BigDecimal tip;

}