package app.system.fidelity.persistence.repository;

import app.system.fidelity.persistence.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByEmail(final String email);

    boolean existsByEmail(final String email);

}
