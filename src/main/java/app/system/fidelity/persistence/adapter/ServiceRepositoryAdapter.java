package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.ServiceRepositoryPort;
import app.system.fidelity.domain.Service;
import app.system.fidelity.persistence.mapper.ServiceMapper;
import app.system.fidelity.persistence.repository.ServiceRepository;
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
@Transactional
@RequiredArgsConstructor
public class ServiceRepositoryAdapter implements ServiceRepositoryPort {

    private final ServiceRepository repository;
    private final ServiceMapper mapper;

    @Override
    public Optional<Service> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Service save(final Service model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save service"));
    }

    @Override
    public List<Service> findAll() {
        return of(repository.findAll())
                .orElse(new ArrayList<>())
                .stream().map(mapper::map).toList();
    }

    @Override
    public void delete(final UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<Service> findAllById(final Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findAllById(ids).stream()
                .map(mapper::map)
                .toList();
    }
}