package app.system.fidelity.web.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SettingsUpdateRequest(
        @NotNull(message = "Número de cortes para corte grátis é obrigatório")
        @Min(value = 1, message = "Número de cortes deve ser no mínimo 1")
        @JsonProperty("haircuts_for_free")
        Integer haircutsForFree
) {
}
