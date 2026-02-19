package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepositoryPort extends BaseRepositoryPort<Product> {

    List<Product> findAllActive();

    void delete(UUID id);

}
