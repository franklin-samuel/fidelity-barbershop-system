package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAppointmentDetailsPagedPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.ProductRepositoryPort;
import app.system.fidelity.core.persistence.ServiceRepositoryPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.AppointmentDetail;
import app.system.fidelity.domain.AppointmentFilterList;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.Product;
import app.system.fidelity.domain.Service;
import app.system.fidelity.domain.User;
import app.system.fidelity.domain.pagination.PageObject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetAppointmentDetailsPagedAdapter implements GetAppointmentDetailsPagedPort {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserRepositoryPort userRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ServiceRepositoryPort serviceRepository;
    private final ProductRepositoryPort productRepository;

    @Override
    public PageObject<AppointmentDetail> execute(final Context context) {
        final AppointmentFilterList filters = context.getData(AppointmentFilterList.class);

        final PageObject<Appointment> appointmentPage = appointmentRepository.findByFilters(
                filters,
                filters.getPaging()
        );

        if (appointmentPage.getContent().isEmpty()) {
            return PageObject.<AppointmentDetail>builder()
                    .content(Collections.emptyList())
                    .page(appointmentPage.getPage())
                    .size(appointmentPage.getSize())
                    .totalElements(0L)
                    .totalPages(0)
                    .sort(appointmentPage.getSort())
                    .direction(appointmentPage.getDirection())
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }

        final List<Appointment> appointments = appointmentPage.getContent();

        final Set<UUID> barberIds = appointments.stream()
                .map(Appointment::getBarberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Set<UUID> customerIds = appointments.stream()
                .map(Appointment::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Set<UUID> serviceIds = appointments.stream()
                .map(Appointment::getServiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Set<UUID> productIds = appointments.stream()
                .map(Appointment::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Map<UUID, String> barberNames = barberIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(barberIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        final Map<UUID, String> customerNames = customerIds.isEmpty()
                ? Collections.emptyMap()
                : customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));

        final Map<UUID, String> serviceNames = serviceIds.isEmpty()
                ? Collections.emptyMap()
                : serviceRepository.findAllById(serviceIds).stream()
                .collect(Collectors.toMap(Service::getId, Service::getName));

        final Map<UUID, String> productNames = productIds.isEmpty()
                ? Collections.emptyMap()
                : productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        final List<AppointmentDetail> enrichedAppointments = appointments.stream()
                .map(appointment -> enrichAppointment(
                        appointment,
                        barberNames,
                        customerNames,
                        serviceNames,
                        productNames
                ))
                .collect(Collectors.toList());

        return PageObject.<AppointmentDetail>builder()
                .content(enrichedAppointments)
                .page(appointmentPage.getPage())
                .size(appointmentPage.getSize())
                .totalElements(appointmentPage.getTotalElements())
                .totalPages(appointmentPage.getTotalPages())
                .sort(appointmentPage.getSort())
                .direction(appointmentPage.getDirection())
                .hasNext(appointmentPage.isHasNext())
                .hasPrevious(appointmentPage.isHasPrevious())
                .build();
    }

    private AppointmentDetail enrichAppointment(
            final Appointment appointment,
            final Map<UUID, String> barberNames,
            final Map<UUID, String> customerNames,
            final Map<UUID, String> serviceNames,
            final Map<UUID, String> productNames
    ) {
        final String barberName = appointment.getBarberId() != null
                ? barberNames.get(appointment.getBarberId())
                : null;

        final String customerName = appointment.getCustomerId() != null
                ? customerNames.get(appointment.getCustomerId())
                : null;

        final String serviceName = appointment.getServiceId() != null
                ? serviceNames.get(appointment.getServiceId())
                : null;

        final String productName = appointment.getProductId() != null
                ? productNames.get(appointment.getProductId())
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