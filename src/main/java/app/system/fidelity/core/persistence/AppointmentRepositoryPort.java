package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Appointment;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepositoryPort extends BaseRepositoryPort<Appointment> {

    List<Appointment> findByBarberId(final UUID barberId);
    long countAll();
    long countByLoyaltyDiscountApplied(final boolean value);

}
