package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.persistence.mapper.UserMapper;
import app.system.fidelity.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.of;
import java.util.UUID;

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
        return of(repository.findAll())
                .orElse(new ArrayList<>())
                .stream()
                .map(mapper::map).toList();
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return repository.findByEmail(email)
                .map(mapper::map);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return repository.existsByEmail(email);
    }

}
