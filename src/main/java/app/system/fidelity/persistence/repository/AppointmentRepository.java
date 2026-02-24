package app.system.fidelity.persistence.repository;

import app.system.fidelity.persistence.model.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    List<AppointmentEntity> findAllByOrderByCreatedAtDesc();
    List<AppointmentEntity> findByBarberIdOrderByCreatedAtDesc(UUID barberId);

    List<AppointmentEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    List<AppointmentEntity> findByBarberIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID barberId, LocalDateTime start, LocalDateTime end);

    long countByLoyaltyDiscountApplied(boolean loyaltyDiscountApplied);
}