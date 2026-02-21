package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Service;
import app.system.fidelity.persistence.model.ServiceEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ServiceMapper {

    Service map(final ServiceEntity source);

    ServiceEntity map(final Service source);

}