package app.system.fidelity.persistence.repository;

import app.system.fidelity.persistence.model.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    List<AppointmentEntity> findAllByOrderByCreatedAtDesc();
    List<AppointmentEntity> findByBarberIdOrderByCreatedAtDesc(UUID barberId);
    long countByLoyaltyDiscountApplied(boolean loyaltyDiscountApplied);
}