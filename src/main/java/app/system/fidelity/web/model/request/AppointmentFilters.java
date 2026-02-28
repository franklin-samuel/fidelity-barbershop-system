package app.system.fidelity.web.model.request;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentFilters {

    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Fortaleza")
    private LocalDateTime startDate;

    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Fortaleza")
    private LocalDateTime endDate;

    private AppointmentType type;

    @JsonProperty("barber_id")
    private UUID barberId;

    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @JsonProperty("search_anything")
    private String searchAnything;

}