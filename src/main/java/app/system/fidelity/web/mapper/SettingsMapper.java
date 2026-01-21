package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.Settings;
import app.system.fidelity.web.model.response.SettingsResponse;
import org.springframework.stereotype.Component;

@Component
public class SettingsMapper {

    public SettingsResponse mapToResponse(final Settings settings) {
        return SettingsResponse.builder()
                .id(settings.getId())
                .haircutsForFree(settings.getHaircuts_for_free())
                .build();
    }
}
