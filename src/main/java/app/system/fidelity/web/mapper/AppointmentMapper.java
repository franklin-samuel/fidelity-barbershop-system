package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.AppointmentDetail;
import app.system.fidelity.web.model.response.AppointmentDetailResponse;
import app.system.fidelity.web.model.response.AppointmentResponse;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse mapToResponse(final Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .barberId(appointment.getBarberId())
                .customerId(appointment.getCustomerId())
                .type(appointment.getType())
                .serviceId(appointment.getServiceId())
                .productId(appointment.getProductId())
                .paymentMethod(appointment.getPaymentMethod())
                .tip(appointment.getTip())
                .price(appointment.getPrice())
                .commissionPercentage(appointment.getCommissionPercentage())
                .commissionAmount(appointment.getCommissionAmount())
                .discountAmount(appointment.getDiscountAmount())
                .totalAmount(appointment.getTotalAmount())
                .loyaltyDiscountApplied(appointment.getLoyaltyDiscountApplied())
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    public AppointmentDetailResponse mapToDetailResponse(final AppointmentDetail detail) {
        final Appointment appointment = detail.getAppointment();

        return AppointmentDetailResponse.builder()
                .id(appointment.getId())
                .barberId(appointment.getBarberId())
                .barberName(detail.getBarberName())
                .customerId(appointment.getCustomerId())
                .customerName(detail.getCustomerName())
                .type(appointment.getType())
                .serviceId(appointment.getServiceId())
                .serviceName(detail.getServiceName())
                .productId(appointment.getProductId())
                .productName(detail.getProductName())
                .paymentMethod(appointment.getPaymentMethod())
                .tip(appointment.getTip())
                .price(appointment.getPrice())
                .commissionPercentage(appointment.getCommissionPercentage())
                .commissionAmount(appointment.getCommissionAmount())
                .discountAmount(appointment.getDiscountAmount())
                .totalAmount(appointment.getTotalAmount())
                .loyaltyDiscountApplied(appointment.getLoyaltyDiscountApplied())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}