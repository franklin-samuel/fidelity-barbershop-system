package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetDashboardMetricsPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.SettingsRepositoryPort;
import app.system.fidelity.domain.DashboardMetrics;
import app.system.fidelity.domain.Settings;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetDashboardMetricsAdapter implements GetDashboardMetricsPort {

    private final CustomerRepositoryPort customerRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final SettingsRepositoryPort settingsRepository;

    @Override
    public DashboardMetrics execute(final Context context) {
        final Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações do sistema não encontradas"));

        final long totalCustomers = customerRepository.countAll();
        final long totalAppointments = appointmentRepository.countAll();
        final long discountsGiven = appointmentRepository.countByLoyaltyDiscountApplied(true);
        final long customersReadyForDiscount = customerRepository
                .countByServiceCountGreaterThanEqual(settings.getHaircutsForFree());

        return DashboardMetrics.builder()
                .totalCustomers(totalCustomers)
                .totalHaircuts(totalAppointments)
                .freeHaircutsGiven(discountsGiven)
                .customersReadyForFreeHaircut(customersReadyForDiscount)
                .build();
    }
}