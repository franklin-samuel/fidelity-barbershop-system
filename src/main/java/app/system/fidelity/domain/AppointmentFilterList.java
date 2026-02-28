package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import app.system.fidelity.domain.pagination.Paging;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
public class AppointmentFilterList {

    private final LocalDateTime startDate;

    private final LocalDateTime endDate;

    private final AppointmentType type;

    private final UUID barberId;

    private final UUID customerId;

    private final PaymentMethod paymentMethod;

    private final String searchAnything;

    private final Paging paging;

}