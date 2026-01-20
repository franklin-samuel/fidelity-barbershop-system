package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.Haircut;
import app.system.fidelity.persistence.model.CustomerEntity;
import app.system.fidelity.persistence.model.HaircutEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HaircutMapper {

    Haircut map(final CustomerEntity source);

    HaircutEntity map(final Customer source);

}
