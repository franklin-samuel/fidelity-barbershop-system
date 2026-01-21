package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Settings;
import app.system.fidelity.persistence.model.SettingsEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SettingsMapper {

    Settings map(final SettingsEntity source);

    SettingsEntity map(final Settings source);

}
