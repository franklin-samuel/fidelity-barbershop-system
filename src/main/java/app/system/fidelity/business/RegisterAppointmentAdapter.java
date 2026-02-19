package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.RegisterAppointmentPort;
import app.system.fidelity.core.persistence.*;
import app.system.fidelity.domain.*;
import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RegisterAppointmentAdapter implements RegisterAppointmentPort {

    private final AppointmentRepositoryPort appointmentRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ServiceRepositoryPort serviceRepository;
    private final ProductRepositoryPort productRepository;
    private final SettingsRepositoryPort settingsRepository;

    @Override
    public Appointment execute(final Context context) {
        final UUID barberId = context.getProperty("barberId", UUID.class);
        final Appointment form = context.getData(Appointment.class);

        if (barberId == null) {
            throw new BusinessException("Usuário não autenticado.");
        }
        if (form == null) {
            throw new BusinessException("Por favor, insira os dados do atendimento.");
        }
        if (form.getType() == null) {
            throw new BusinessException("Por favor, informe o tipo do atendimento.");
        }
        if (form.getPaymentMethod() == null) {
            throw new BusinessException("Por favor, selecione o meio de pagamento.");
        }

        final BigDecimal tip = form.getTip() != null ? form.getTip() : BigDecimal.ZERO;

        if (form.getType() == AppointmentType.SERVICE) {
            return registerServiceAppointment(form, barberId, tip);
        } else {
            return registerProductAppointment(form, barberId, tip);
        }
    }

    private Appointment registerServiceAppointment(
            final Appointment form,
            final UUID barberId,
            final BigDecimal tip
    ) {
        if (form.getServiceId() == null) {
            throw new BusinessException("Por favor, selecione o serviço.");
        }

        final app.system.fidelity.domain.Service service = serviceRepository.get(form.getServiceId())
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        if (!service.getActive()) {
            throw new BusinessException("O serviço '" + service.getName() + "' está inativo.");
        }

        final Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações do sistema não encontradas."));

        UUID customerId = null;
        boolean loyaltyDiscountApplied = false;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (form.getCustomerId() != null) {
            final Customer customer = customerRepository.get(form.getCustomerId())
                    .orElseThrow(() -> new BusinessException("Cliente não encontrado."));

            customerId = customer.getId();

            if (customer.getServiceCount() >= settings.getHaircutsForFree()) {
                discountAmount = service.getPrice()
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                loyaltyDiscountApplied = true;
                customer.setServiceCount(0);
                customer.setDiscountsClaimed(customer.getDiscountsClaimed() + 1);
            } else {
                customer.setServiceCount(customer.getServiceCount() + 1);
            }

            customer.setModifiedAt(LocalDateTime.now());
            customerRepository.save(customer);
        }

        final BigDecimal totalAmount = service.getPrice().subtract(discountAmount);
        final BigDecimal commissionBase = totalAmount.add(tip);
        final BigDecimal commissionAmount = commissionBase
                .multiply(service.getCommissionPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return appointmentRepository.save(Appointment.builder()
                .barberId(barberId)
                .customerId(customerId)
                .type(AppointmentType.SERVICE)
                .serviceId(service.getId())
                .paymentMethod(form.getPaymentMethod())
                .tip(tip)
                .price(service.getPrice())
                .commissionPercentage(service.getCommissionPercentage())
                .commissionAmount(commissionAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .loyaltyDiscountApplied(loyaltyDiscountApplied)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Appointment registerProductAppointment(
            final Appointment form,
            final UUID barberId,
            final BigDecimal tip
    ) {
        if (form.getProductId() == null) {
            throw new BusinessException("Por favor, selecione o produto.");
        }

        final Product product = productRepository.get(form.getProductId())
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));

        if (!product.getActive()) {
            throw new BusinessException("O produto '" + product.getName() + "' está inativo.");
        }

        final BigDecimal commissionBase = product.getPrice().add(tip);
        final BigDecimal commissionAmount = commissionBase
                .multiply(product.getCommissionPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return appointmentRepository.save(Appointment.builder()
                .barberId(barberId)
                .customerId(null)
                .type(AppointmentType.PRODUCT)
                .productId(product.getId())
                .paymentMethod(form.getPaymentMethod())
                .tip(tip)
                .price(product.getPrice())
                .commissionPercentage(product.getCommissionPercentage())
                .commissionAmount(commissionAmount)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(product.getPrice())
                .loyaltyDiscountApplied(false)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}