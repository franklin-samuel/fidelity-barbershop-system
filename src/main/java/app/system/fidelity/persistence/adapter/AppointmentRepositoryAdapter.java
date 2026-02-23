package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.persistence.mapper.AppointmentMapper;
import app.system.fidelity.persistence.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.of;

@Repository
@Transactional
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {

    private final AppointmentRepository repository;
    private final AppointmentMapper mapper;

    @Override
    public Optional<Appointment> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Appointment save(final Appointment model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save appointment"));
    }

    @Override
    public List<Appointment> findAll() {
        return of(repository.findAllByOrderByCreatedAtDesc())
                .orElse(new ArrayList<>())
                .stream().map(mapper::map).toList();
    }

    @Override
    public List<Appointment> findByBarberId(final UUID barberId) {
        return repository.findByBarberIdOrderByCreatedAtDesc(barberId).stream().map(mapper::map).toList();
    }

    @Override
    public long countAll() {
        return repository.count();
    }

    @Override
    public long countByLoyaltyDiscountApplied(final boolean value) {
        return repository.countByLoyaltyDiscountApplied(value);
    }
}