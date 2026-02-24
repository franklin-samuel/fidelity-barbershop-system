package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.BarberRevenueSummary;
import app.system.fidelity.domain.CustomerAppointmentSummary;
import app.system.fidelity.persistence.mapper.AppointmentMapper;
import app.system.fidelity.persistence.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<Appointment> findByBarberId(final UUID barberId) {
        return repository.findByBarberIdOrderByCreatedAtDesc(barberId)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<Appointment> findByCreatedAtBetween(final LocalDateTime start, final LocalDateTime end) {
        return repository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<Appointment> findByBarberIdAndCreatedAtBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end) {
        return repository.findByBarberIdAndCreatedAtBetweenOrderByCreatedAtDesc(barberId, start, end)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public long countAll() {
        return repository.count();
    }

    @Override
    public long countByLoyaltyDiscountApplied(final boolean value) {
        return repository.countByLoyaltyDiscountApplied(value);
    }

    @Override
    public BigDecimal sumTotalAmount() {
        return repository.sumTotalAmount();
    }

    @Override
    public BigDecimal sumTotalAmountWithCustomer() {
        return repository.sumTotalAmountWithCustomer();
    }

    @Override
    public long countWithCustomer() {
        return repository.countWithCustomer();
    }

    @Override
    public BigDecimal sumBarbershopRevenueBetween(final LocalDateTime start, final LocalDateTime end) {
        return repository.sumBarbershopRevenueBetween(start, end);
    }

    @Override
    public BigDecimal sumServicesRevenueBetween(final LocalDateTime start, final LocalDateTime end) {
        return repository.sumServicesRevenueBetween(start, end);
    }

    @Override
    public BigDecimal sumProductsRevenueBetween(final LocalDateTime start, final LocalDateTime end) {
        return repository.sumProductsRevenueBetween(start, end);
    }

    @Override
    public List<BarberRevenueSummary> findTopBarbersByRevenueBetween(final LocalDateTime start, final LocalDateTime end) {
        return repository.findTopBarbersByRevenueBetween(start, end, PageRequest.of(0, 5))
                .stream()
                .map(row -> new BarberRevenueSummary(
                        (UUID) row[0],
                        (BigDecimal) row[1],
                        (Long) row[2]
                ))
                .toList();
    }

    @Override
    public List<CustomerAppointmentSummary> findRevenueGroupByCustomer() {
        return repository.findRevenueGroupByCustomer()
                .stream()
                .map(row -> new CustomerAppointmentSummary(
                        (UUID) row[0],
                        (BigDecimal) row[1],
                        (Long) row[2]
                ))
                .toList();
    }

    @Override
    public long countDistinctCustomersBetween(final LocalDateTime start, final LocalDateTime end) {
        return repository.countDistinctCustomersBetween(start, end);
    }

    @Override
    public long countRetainedCustomers(
            final LocalDateTime previousStart,
            final LocalDateTime previousEnd,
            final LocalDateTime currentStart,
            final LocalDateTime currentEnd
    ) {
        return repository.countRetainedCustomers(previousStart, previousEnd, currentStart, currentEnd);
    }

    @Override
    public BigDecimal sumBarberTotalByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end) {
        return repository.sumBarberTotalByBarberIdBetween(barberId, start, end);
    }

    @Override
    public BigDecimal sumTotalAmountByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end) {
        return repository.sumTotalAmountByBarberIdBetween(barberId, start, end);
    }

    @Override
    public long countByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end) {
        return repository.countByBarberIdBetween(barberId, start, end);
    }

    @Override
    public BigDecimal sumTipsByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end) {
        return repository.sumTipsByBarberIdBetween(barberId, start, end);
    }

    @Override
    public BigDecimal sumCommissionByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end) {
        return repository.sumCommissionByBarberIdBetween(barberId, start, end);
    }

}