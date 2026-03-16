package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.RegisterAppointmentPort;
import app.system.fidelity.core.persistence.*;
import app.system.fidelity.domain.*;
import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.Role;
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
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public Appointment execute(final Context context) {
        final UUID authenticatedUserId = context.getProperty("authenticatedUserId", UUID.class);
        final Role authenticatedUserRole = context.getProperty("authenticatedUserRole", Role.class);
        final Appointment form = context.getData(Appointment.class);

        if (authenticatedUserId == null) {
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

        final UUID barberId = determineBarberId(form.getBarberId(), authenticatedUserId, authenticatedUserRole);

        final BigDecimal tip = form.getTip() != null ? form.getTip() : BigDecimal.ZERO;

        if (form.getType() == AppointmentType.SERVICE) {
            return registerServiceAppointment(form, barberId, tip);
        } else {
            return registerProductAppointment(form, barberId, tip);
        }
    }

    private UUID determineBarberId(final UUID requestBarberId, final UUID authenticatedUserId, final Role authenticatedUserRole) {
        if (authenticatedUserRole == Role.BARBER) {
            return authenticatedUserId;
        } else if (authenticatedUserRole == Role.ADMIN) {
            if (requestBarberId == null) {
                throw new BusinessException("Administrador deve informar qual barbeiro realizou o atendimento.");
            }

            final User barber = userRepositoryPort.get(requestBarberId)
                    .orElseThrow(() -> new BusinessException("Barbeiro não encontrado."));

            if (barber.getRole() != Role.BARBER) {
                throw new BusinessException("O usuário informado não é um barbeiro.");
            }

            if (barber.getDeletedAt() != null) {
                throw new BusinessException("O barbeiro informado está inativo.");
            }

            return requestBarberId;
        } else {
            throw new BusinessException("Role de usuário não reconhecida.");
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

        final Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações do sistema não encontradas."));

        final User barber = userRepositoryPort.get(barberId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        final boolean isAdmin = barber.getRole() == Role.ADMIN;

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

            customer.setLastVisitDate(LocalDateTime.now());

            final BigDecimal totalAmount = service.getPrice().subtract(discountAmount);
            final BigDecimal currentTotalSpent = customer.getTotalSpent() != null
                    ? customer.getTotalSpent()
                    : BigDecimal.ZERO;
            customer.setTotalSpent(currentTotalSpent.add(totalAmount));

            customer.setModifiedAt(LocalDateTime.now());
            customerRepository.save(customer);
        }

        final BigDecimal totalAmount = service.getPrice().subtract(discountAmount);

        final BigDecimal commissionAmount;
        final BigDecimal barberTotal;
        final BigDecimal barbershopRevenue;

        if (isAdmin) {
            commissionAmount = BigDecimal.ZERO;
            barberTotal = BigDecimal.ZERO;
            barbershopRevenue = totalAmount.add(tip);
        } else {
            commissionAmount = totalAmount
                    .multiply(service.getCommissionPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            barberTotal = commissionAmount.add(tip);
            barbershopRevenue = totalAmount.subtract(commissionAmount);
        }

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
                .barberTotal(barberTotal)
                .barbershopRevenue(barbershopRevenue)
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

        final User barber = userRepositoryPort.get(barberId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        final boolean isAdmin = barber.getRole() == Role.ADMIN;

        if (form.getCustomerId() != null) {
            final Customer customer = customerRepository.get(form.getCustomerId())
                    .orElseThrow(() -> new BusinessException("Cliente não encontrado."));

            customer.setLastVisitDate(LocalDateTime.now());

            final BigDecimal currentTotalSpent = customer.getTotalSpent() != null
                    ? customer.getTotalSpent()
                    : BigDecimal.ZERO;
            customer.setTotalSpent(currentTotalSpent.add(product.getPrice()));

            customer.setModifiedAt(LocalDateTime.now());
            customerRepository.save(customer);
        }

        final BigDecimal commissionAmount;
        final BigDecimal barberTotal;
        final BigDecimal barbershopRevenue;

        if (isAdmin) {
            commissionAmount = BigDecimal.ZERO;
            barberTotal = BigDecimal.ZERO;
            barbershopRevenue = product.getPrice().add(tip);
        } else {
            commissionAmount = product.getPrice()
                    .multiply(product.getCommissionPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            barberTotal = commissionAmount.add(tip);
            barbershopRevenue = product.getPrice().subtract(commissionAmount);
        }

        return appointmentRepository.save(Appointment.builder()
                .barberId(barberId)
                .customerId(form.getCustomerId())
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
                .barberTotal(barberTotal)
                .barbershopRevenue(barbershopRevenue)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}