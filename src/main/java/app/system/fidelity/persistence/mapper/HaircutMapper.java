package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Haircut;
import app.system.fidelity.persistence.model.HaircutEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface HaircutMapper {

    @Mapping(target = "customer_id", source = "customer_id.id")
    @Mapping(target = "registered_by", source = "registered_by.id")
    Haircut map(HaircutEntity source);

    @Mapping(target = "customer_id.id", source = "customer_id")
    @Mapping(target = "registered_by.id", source = "registered_by")
    HaircutEntity map(Haircut source);

}
