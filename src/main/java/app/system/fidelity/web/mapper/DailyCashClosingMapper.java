package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.DailyCashClosing;
import app.system.fidelity.web.model.response.DailyCashClosingResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DailyCashClosingMapper {

    public DailyCashClosingResponse mapToResponse(final DailyCashClosing cashClosing) {
        return DailyCashClosingResponse.builder()
                .date(cashClosing.getDate())
                .period(DailyCashClosingResponse.PeriodResponse.builder()
                        .start(cashClosing.getPeriodStart())
                        .end(cashClosing.getPeriodEnd())
                        .build())
                .totalRevenue(cashClosing.getTotalRevenue())
                .totalAppointments(cashClosing.getTotalAppointments())
                .byPaymentMethod(cashClosing.getByPaymentMethod() != null
                        ? cashClosing.getByPaymentMethod().stream()
                        .map(summary -> DailyCashClosingResponse.PaymentMethodSummaryResponse.builder()
                                .paymentMethod(summary.getPaymentMethod())
                                .revenue(summary.getRevenue())
                                .appointments(summary.getAppointments())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .build();
    }
}