package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Customer;
import app.system.fidelity.persistence.model.CustomerEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(builder = @Builder(disableBuilder = true))
public interface CustomerMapper {

    Customer map(final CustomerEntity source);

    CustomerEntity map(final Customer source);

}
