package app.system.fidelity.web.model.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


import java.util.UUID;

@Builder
public record SettingsResponse(
        UUID id,

        @JsonProperty("haircuts_for_free")
        Integer haircutsForFree
) {}
