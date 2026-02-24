package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.domain.enums.Role;
import app.system.fidelity.persistence.mapper.UserMapper;
import app.system.fidelity.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.of;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public Optional<User> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public User save(final User model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save user"));
    }

    @Override
    public List<User> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<User> findAllBarbers() {
        return repository.findByRole(Role.BARBER)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<User> findAllAdmins() {
        return repository.findByRole(Role.ADMIN)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return repository.findByEmail(email).map(mapper::map);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public List<User> findAllById(final Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findAllById(ids)
                .stream()
                .map(mapper::map)
                .toList();
    }

}