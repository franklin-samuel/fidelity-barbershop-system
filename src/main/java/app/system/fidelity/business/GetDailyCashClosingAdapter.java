package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetDailyCashClosingPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.domain.DailyCashClosing;
import app.system.fidelity.domain.PaymentMethodRevenue;
import app.system.fidelity.domain.PaymentMethodSummary;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetDailyCashClosingAdapter implements GetDailyCashClosingPort {

    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    public DailyCashClosing execute(final Context context) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        final LocalDateTime endOfDay = now;

        final List<PaymentMethodRevenue> paymentMethodData = appointmentRepository
                .findRevenueGroupByPaymentMethodBetween(startOfDay, endOfDay);

        final BigDecimal totalRevenue = paymentMethodData.stream()
                .map(PaymentMethodRevenue::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Long totalAppointments = paymentMethodData.stream()
                .map(PaymentMethodRevenue::appointments)
                .reduce(0L, Long::sum);

        final List<PaymentMethodSummary> summaries = paymentMethodData.stream()
                .map(data -> PaymentMethodSummary.builder()
                        .paymentMethod(data.paymentMethod() != null ? data.paymentMethod().name() : "UNKNOWN")
                        .revenue(data.revenue())
                        .appointments(data.appointments())
                        .build())
                .collect(Collectors.toList());

        return DailyCashClosing.builder()
                .date(LocalDate.now())
                .periodStart(startOfDay)
                .periodEnd(endOfDay)
                .totalRevenue(totalRevenue)
                .totalAppointments(totalAppointments)
                .byPaymentMethod(summaries)
                .build();
    }
}