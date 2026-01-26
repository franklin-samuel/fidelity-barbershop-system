package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort extends BaseRepositoryPort<User> {

    Optional<User> findByEmail(final String email);

    boolean existsByEmail(final String email);

    void delete(final UUID id);

}
