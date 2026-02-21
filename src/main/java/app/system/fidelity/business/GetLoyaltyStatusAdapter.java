package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetLoyaltyStatusPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.ServiceRepositoryPort;
import app.system.fidelity.core.persistence.SettingsRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.LoyaltyStatus;
import app.system.fidelity.domain.Settings;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetLoyaltyStatusAdapter implements GetLoyaltyStatusPort {

    private final CustomerRepositoryPort customerRepository;
    private final ServiceRepositoryPort serviceRepository;
    private final SettingsRepositoryPort settingsRepository;

    @Override
    public LoyaltyStatus execute(final Context context) {
        final UUID customerId = context.getProperty("customerId", UUID.class);
        final UUID serviceId = context.getProperty("serviceId", UUID.class);

        if (customerId == null) {
            throw new BusinessException("Cliente não encontrado.");
        }

        final Customer customer = customerRepository.get(customerId)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado."));

        final Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações do sistema não encontradas."));

        final boolean hasDiscount = customer.getServiceCount() >= settings.getHaircutsForFree();

        if (serviceId == null) {
            return LoyaltyStatus.builder()
                    .hasDiscount(hasDiscount)
                    .serviceCount(customer.getServiceCount())
                    .discountsClaimed(customer.getDiscountsClaimed())
                    .originalPrice(null)
                    .discountAmount(null)
                    .totalAmount(null)
                    .build();
        }

        final app.system.fidelity.domain.Service service = serviceRepository.get(serviceId)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        final BigDecimal originalPrice = service.getPrice();
        final BigDecimal discountAmount = hasDiscount
                ? originalPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        final BigDecimal totalAmount = originalPrice.subtract(discountAmount);

        return LoyaltyStatus.builder()
                .hasDiscount(hasDiscount)
                .serviceCount(customer.getServiceCount())
                .discountsClaimed(customer.getDiscountsClaimed())
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .build();
    }
}