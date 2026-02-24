package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.BarberRevenueSummary;
import app.system.fidelity.domain.CustomerAppointmentSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepositoryPort extends BaseRepositoryPort<Appointment> {

    List<Appointment> findByBarberId(final UUID barberId);

    List<Appointment> findByCreatedAtBetween(final LocalDateTime start, final LocalDateTime end);

    List<Appointment> findByBarberIdAndCreatedAtBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end);

    long countAll();

    long countByLoyaltyDiscountApplied(final boolean value);

    BigDecimal sumTotalAmount();

    BigDecimal sumTotalAmountWithCustomer();

    long countWithCustomer();

    BigDecimal sumBarbershopRevenueBetween(final LocalDateTime start, final LocalDateTime end);

    BigDecimal sumServicesRevenueBetween(final LocalDateTime start, final LocalDateTime end);

    BigDecimal sumProductsRevenueBetween(final LocalDateTime start, final LocalDateTime end);

    List<BarberRevenueSummary> findTopBarbersByRevenueBetween(final LocalDateTime start, final LocalDateTime end);

    List<CustomerAppointmentSummary> findRevenueGroupByCustomer();

    long countDistinctCustomersBetween(final LocalDateTime start, final LocalDateTime end);

    long countRetainedCustomers(
            final LocalDateTime previousStart,
            final LocalDateTime previousEnd,
            final LocalDateTime currentStart,
            final LocalDateTime currentEnd
    );

    BigDecimal sumBarberTotalByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end);

    BigDecimal sumTotalAmountByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end);

    long countByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end);

    BigDecimal sumTipsByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end);

    BigDecimal sumCommissionByBarberIdBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end);

}