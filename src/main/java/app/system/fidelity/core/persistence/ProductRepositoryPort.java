package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Product;

import java.util.List;

public interface ProductRepositoryPort extends BaseRepositoryPort<Product> {

    List<Product> findAllActive();

}
