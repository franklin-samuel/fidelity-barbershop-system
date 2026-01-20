package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Customer;

import java.util.Optional;

public interface CustomerRepositoryPort extends BaseRepositoryPort<Customer> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

}
