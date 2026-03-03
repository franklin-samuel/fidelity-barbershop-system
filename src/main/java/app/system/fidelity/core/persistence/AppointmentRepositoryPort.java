package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.AppointmentFilterList;
import app.system.fidelity.domain.BarberRevenueSummary;
import app.system.fidelity.domain.CustomerAppointmentSummary;
import app.system.fidelity.domain.WeekdayRevenue;
import app.system.fidelity.domain.pagination.PageObject;
import app.system.fidelity.domain.pagination.Paging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepositoryPort extends BaseRepositoryPort<Appointment> {

    List<Appointment> findByBarberId(final UUID barberId);

    List<Appointment> findByCreatedAtBetween(final LocalDateTime start, final LocalDateTime end);

    List<Appointment> findByBarberIdAndCreatedAtBetween(final UUID barberId, final LocalDateTime start, final LocalDateTime end);

    long countAll();

    long countByLoyaltyDiscountApplied(final boolean value);

    BigDecimal sumTotalAmount();

    BigDecimal sumTotalAmountBetween(final LocalDateTime start, final LocalDateTime end);

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

    List<WeekdayRevenue> findRevenueByWeekdayFrom(final LocalDateTime startDate);

    BigDecimal sumTotalAmountWithCustomerBetween(final LocalDateTime start, final LocalDateTime end);

    long countWithCustomerBetween(final LocalDateTime start, final LocalDateTime end);

    Optional<LocalDateTime> findFirstAppointmentDateFrom(final LocalDateTime startDate);

    Optional<LocalDateTime> findLastAppointmentDateFrom(final LocalDateTime startDate);

    PageObject<Appointment> findByFilters(final AppointmentFilterList filters, final Paging paging);

}