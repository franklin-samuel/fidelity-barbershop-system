package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Product;
import app.system.fidelity.persistence.model.ProductEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProductMapper {

    Product map(final ProductEntity source);

    ProductEntity map(final Product source);

}