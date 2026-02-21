package app.system.fidelity.web.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AppointmentUpdateRequest(

        @NotNull(message = "Cliente é obrigatório")
        @JsonProperty("customer_id")
        UUID customerId

) {}