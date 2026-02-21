package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAppointmentDetailsPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.ProductRepositoryPort;
import app.system.fidelity.core.persistence.ServiceRepositoryPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.AppointmentDetail;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.Product;
import app.system.fidelity.domain.Service;
import app.system.fidelity.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetAppointmentDetailsAdapter implements GetAppointmentDetailsPort {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserRepositoryPort userRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ServiceRepositoryPort serviceRepository;
    private final ProductRepositoryPort productRepository;

    @Override
    public List<AppointmentDetail> execute(final Context context) {
        final UUID barberId = context.getProperty("barberId", UUID.class);

        final List<Appointment> appointments = barberId != null
                ? appointmentRepository.findByBarberId(barberId)
                : appointmentRepository.findAll();

        return appointments.stream()
                .map(this::enrichAppointment)
                .collect(Collectors.toList());
    }

    private AppointmentDetail enrichAppointment(final Appointment appointment) {
        final String barberName = appointment.getBarberId() != null
                ? userRepository.get(appointment.getBarberId())
                .map(User::getName)
                .orElse(null)
                : null;

        final String customerName = appointment.getCustomerId() != null
                ? customerRepository.get(appointment.getCustomerId())
                .map(Customer::getName)
                .orElse(null)
                : null;

        final String serviceName = appointment.getServiceId() != null
                ? serviceRepository.get(appointment.getServiceId())
                .map(Service::getName)
                .orElse(null)
                : null;

        final String productName = appointment.getProductId() != null
                ? productRepository.get(appointment.getProductId())
                .map(Product::getName)
                .orElse(null)
                : null;

        return AppointmentDetail.builder()
                .appointment(appointment)
                .barberName(barberName)
                .customerName(customerName)
                .serviceName(serviceName)
                .productName(productName)
                .build();
    }
}