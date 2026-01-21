package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.HaircutRepositoryPort;
import app.system.fidelity.domain.Haircut;
import app.system.fidelity.persistence.mapper.HaircutMapper;
import app.system.fidelity.persistence.repository.HaircutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.of;

@Repository
@Transactional
@RequiredArgsConstructor
public class HaircutRepositoryAdapter implements HaircutRepositoryPort {

    private final HaircutRepository repository;
    private HaircutMapper mapper;

    @Override
    public Optional<Haircut> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Haircut save(final Haircut model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save settings"));
    }

    @Override
    public List<Haircut> findAll() {
        return of(repository.findAll())
                .orElse(new ArrayList<>())
                .stream()
                .map(mapper::map).toList();
    }

}
