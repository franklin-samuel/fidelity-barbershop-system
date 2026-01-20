package app.system.fidelity.persistence.repository;

import app.system.fidelity.persistence.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(final String email);

    boolean existsByEmail(final String email);

}
