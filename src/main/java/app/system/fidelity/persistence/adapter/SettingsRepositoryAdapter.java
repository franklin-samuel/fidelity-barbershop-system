package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.SettingsRepositoryPort;
import app.system.fidelity.domain.Settings;
import app.system.fidelity.domain.User;
import app.system.fidelity.persistence.mapper.SettingsMapper;
import app.system.fidelity.persistence.repository.SettingsRepository;
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
public class SettingsRepositoryAdapter implements SettingsRepositoryPort {

    private final SettingsRepository repository;
    private final SettingsMapper mapper;

    @Override
    public Optional<Settings> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Settings save(final Settings model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save settings"));
    }

    @Override
    public List<Settings> findAll() {
        return of(repository.findAll())
                .orElse(new ArrayList<>())
                .stream()
                .map(mapper::map).toList();
    }

}
