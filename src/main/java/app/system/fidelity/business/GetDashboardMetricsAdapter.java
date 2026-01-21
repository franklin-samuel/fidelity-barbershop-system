package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetDashboardMetricsPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.HaircutRepositoryPort;
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
    private final HaircutRepositoryPort haircutRepository;
    private final SettingsRepositoryPort settingsRepository;

    @Override
    public DashboardMetrics execute(final Context context) {

        Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações do sistema não encontradas"));

        long totalCustomers = customerRepository.countAll();

        long totalHaircuts = haircutRepository.countAll();

        long freeHaircutsGiven = haircutRepository.countByIsFree(true);

        long customersReadyForFreeHaircut = customerRepository
                .countByHaircutCountGreaterThanEqual(settings.getHaircuts_for_free());

        return DashboardMetrics.builder()
                .totalCustomers(totalCustomers)
                .totalHaircuts(totalHaircuts)
                .freeHaircutsGiven(freeHaircutsGiven)
                .customersReadyForFreeHaircut(customersReadyForFreeHaircut)
                .build();
    }
}