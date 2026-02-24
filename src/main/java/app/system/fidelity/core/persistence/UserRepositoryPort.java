package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepositoryPort extends BaseRepositoryPort<User> {

    Optional<User> findByEmail(final String email);

    boolean existsByEmail(final String email);

    List<User> findAllBarbers();

    List<User> findAllAdmins();

    List<User> findAllById(final Set<UUID> ids);

}