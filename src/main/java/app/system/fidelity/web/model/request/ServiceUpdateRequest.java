package app.system.fidelity.web.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ServiceUpdateRequest(

        String name,

        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal price,

        @DecimalMin(value = "0.0", message = "Porcentagem de comissão não pode ser negativa")
        @JsonProperty("commission_percentage")
        BigDecimal commissionPercentage

) {}