package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.User;
import app.system.fidelity.persistence.mapper.CustomerMapper;
import app.system.fidelity.persistence.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.of;

@Repository
@RequiredArgsConstructor
@Transactional
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public Optional<Customer> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Customer save(final Customer model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save user"));
    }

    @Override
    public List<Customer> findAll() {
        return of(repository.findAll())
                .orElse(new ArrayList<>())
                .stream()
                .map(mapper::map).toList();
    }

    @Override
    public Optional<Customer> findByEmail(final String email) {
        return repository.findByEmail(email)
                .map(mapper::map);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return repository.existsByEmail(email);
    }

}
