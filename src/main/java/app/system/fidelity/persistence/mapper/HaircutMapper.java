package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Haircut;
import app.system.fidelity.persistence.model.HaircutEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface HaircutMapper {

    Haircut map(final HaircutEntity source);

    HaircutEntity map(final Haircut source);

}
