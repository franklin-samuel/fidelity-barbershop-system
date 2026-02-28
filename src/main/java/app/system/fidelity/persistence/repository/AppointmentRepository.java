package app.system.fidelity.persistence.repository;

import app.system.fidelity.persistence.model.AppointmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    List<AppointmentEntity> findAllByOrderByCreatedAtDesc();

    List<AppointmentEntity> findByBarberIdOrderByCreatedAtDesc(UUID barberId);

    List<AppointmentEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<AppointmentEntity> findByBarberIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID barberId, LocalDateTime start, LocalDateTime end);

    long countByLoyaltyDiscountApplied(boolean loyaltyDiscountApplied);

    @Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AppointmentEntity a")
    BigDecimal sumTotalAmount();

    @Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AppointmentEntity a WHERE a.customerId IS NOT NULL")
    BigDecimal sumTotalAmountWithCustomer();

    @Query("SELECT COUNT(a) FROM AppointmentEntity a WHERE a.customerId IS NOT NULL")
    long countWithCustomer();

    @Query("SELECT COALESCE(SUM(a.barbershopRevenue), 0) FROM AppointmentEntity a " +
            "WHERE a.createdAt BETWEEN :start AND :end")
    BigDecimal sumBarbershopRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(a.barbershopRevenue), 0) FROM AppointmentEntity a " +
            "WHERE a.serviceId IS NOT NULL AND a.createdAt BETWEEN :start AND :end")
    BigDecimal sumServicesRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(a.barbershopRevenue), 0) FROM AppointmentEntity a " +
            "WHERE a.productId IS NOT NULL AND a.createdAt BETWEEN :start AND :end")
    BigDecimal sumProductsRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT a.barberId, SUM(a.barbershopRevenue), COUNT(a) FROM AppointmentEntity a " +
            "WHERE a.createdAt BETWEEN :start AND :end " +
            "GROUP BY a.barberId " +
            "ORDER BY SUM(a.barbershopRevenue) DESC")
    List<Object[]> findTopBarbersByRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("SELECT a.customerId, SUM(a.totalAmount), COUNT(a) FROM AppointmentEntity a " +
            "WHERE a.customerId IS NOT NULL " +
            "GROUP BY a.customerId")
    List<Object[]> findRevenueGroupByCustomer();

    @Query("SELECT COUNT(DISTINCT a.customerId) FROM AppointmentEntity a " +
            "WHERE a.customerId IS NOT NULL " +
            "AND a.createdAt BETWEEN :start AND :end")
    long countDistinctCustomersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(DISTINCT a.customerId) FROM AppointmentEntity a " +
            "WHERE a.customerId IS NOT NULL " +
            "AND a.createdAt BETWEEN :currentStart AND :currentEnd " +
            "AND a.customerId IN (" +
            "  SELECT DISTINCT a2.customerId FROM AppointmentEntity a2 " +
            "  WHERE a2.customerId IS NOT NULL " +
            "  AND a2.createdAt BETWEEN :previousStart AND :previousEnd" +
            ")")
    long countRetainedCustomers(
            @Param("previousStart") LocalDateTime previousStart,
            @Param("previousEnd") LocalDateTime previousEnd,
            @Param("currentStart") LocalDateTime currentStart,
            @Param("currentEnd") LocalDateTime currentEnd
    );

    @Query("SELECT COALESCE(SUM(a.barberTotal), 0) FROM AppointmentEntity a " +
            "WHERE a.barberId = :barberId AND a.createdAt BETWEEN :start AND :end")
    BigDecimal sumBarberTotalByBarberIdBetween(
            @Param("barberId") UUID barberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AppointmentEntity a " +
            "WHERE a.barberId = :barberId AND a.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalAmountByBarberIdBetween(
            @Param("barberId") UUID barberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(a) FROM AppointmentEntity a " +
            "WHERE a.barberId = :barberId AND a.createdAt BETWEEN :start AND :end")
    long countByBarberIdBetween(
            @Param("barberId") UUID barberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(a.tip), 0) FROM AppointmentEntity a " +
            "WHERE a.barberId = :barberId AND a.createdAt BETWEEN :start AND :end")
    BigDecimal sumTipsByBarberIdBetween(
            @Param("barberId") UUID barberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(a.commissionAmount), 0) FROM AppointmentEntity a " +
            "WHERE a.barberId = :barberId AND a.createdAt BETWEEN :start AND :end")
    BigDecimal sumCommissionByBarberIdBetween(
            @Param("barberId") UUID barberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT EXTRACT(DOW FROM a.created_at) as day_of_week, " +
            "SUM(a.total_amount) as total_revenue, " +
            "COUNT(a.id) as appointment_count " +
            "FROM appointments a " +
            "WHERE a.created_at >= :startDate " +
            "GROUP BY EXTRACT(DOW FROM a.created_at)",
            nativeQuery = true)
    List<Object[]> sumRevenueAndCountByWeekdayFrom(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM AppointmentEntity a " +
            "WHERE a.customerId IS NOT NULL AND a.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalAmountWithCustomerBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(a) FROM AppointmentEntity a " +
            "WHERE a.customerId IS NOT NULL AND a.createdAt BETWEEN :start AND :end")
    long countWithCustomerBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT MIN(a.createdAt) FROM AppointmentEntity a WHERE a.createdAt >= :startDate")
    Optional<LocalDateTime> findFirstAppointmentDateFrom(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT MAX(a.createdAt) FROM AppointmentEntity a WHERE a.createdAt >= :startDate")
    Optional<LocalDateTime> findLastAppointmentDateFrom(@Param("startDate") LocalDateTime startDate);

    @NonNull
    Page<AppointmentEntity> findAll(@NonNull final Specification<AppointmentEntity> spec, @NonNull final Pageable pageable);
}