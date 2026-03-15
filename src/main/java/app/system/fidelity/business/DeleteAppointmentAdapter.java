package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteAppointmentPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteAppointmentAdapter implements DeleteAppointmentPort {

    private final AppointmentRepositoryPort appointmentRepository;
    private final CustomerRepositoryPort customerRepository;

    @Override
    public Void execute(final Context context) {

        final UUID appointmentId = context.getData(UUID.class);

        if (appointmentId == null) {
            throw new BusinessException("Atendimento não encontrado");
        }

        final Appointment appointment = appointmentRepository.get(appointmentId)
                .orElseThrow(() -> new BusinessException("Atendimento não encontrado"));

        final UUID customerId = appointment.getCustomerId();

        if (customerId != null) {
            final Customer customer = customerRepository.get(customerId)
                    .orElseThrow(() -> new BusinessException("Cliente não encontrado"));

            if (customer.getServiceCount() > 0) {
                customer.setServiceCount(customer.getServiceCount() - 1);
            }

            if (appointment.getLoyaltyDiscountApplied() && customer.getDiscountsClaimed() > 0) {
                customer.setDiscountsClaimed(customer.getDiscountsClaimed() - 1);
            }

            customerRepository.save(customer);
        }

        appointmentRepository.delete(appointmentId);

        return null;
    }
}