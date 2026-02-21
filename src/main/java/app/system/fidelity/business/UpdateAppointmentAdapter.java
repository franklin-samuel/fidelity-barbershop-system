package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.UpdateAppointmentPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class UpdateAppointmentAdapter implements UpdateAppointmentPort {

    private final AppointmentRepositoryPort appointmentRepository;
    private final CustomerRepositoryPort customerRepository;

    @Override
    public Appointment execute(final Context context) {
        final UUID appointmentId = context.getProperty("appointmentId", UUID.class);
        final UUID customerId = context.getProperty("customerId", UUID.class);

        if (appointmentId == null) {
            throw new BusinessException("Atendimento não encontrado.");
        }

        if (customerId == null) {
            throw new BusinessException("Por favor, informe o cliente.");
        }

        final Appointment appointment = appointmentRepository.get(appointmentId)
                .orElseThrow(() -> new BusinessException("Atendimento não encontrado."));

        if (appointment.getCustomerId() != null) {
            throw new BusinessException("Este atendimento já possui um cliente vinculado.");
        }

        final Customer customer = customerRepository.get(customerId)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado."));

        if (appointment.getType() == AppointmentType.SERVICE) {
            customer.setServiceCount(customer.getServiceCount() + 1);
            customer.setModifiedAt(LocalDateTime.now());
            customerRepository.save(customer);
        }

        appointment.setCustomerId(customer.getId());
        appointment.setModifiedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }
}