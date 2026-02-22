package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Appointment extends AbstractDomain {

    private UUID barberId;
    private UUID customerId;

    private AppointmentType type;

    private UUID serviceId;
    private UUID productId;

    private PaymentMethod paymentMethod;
    private BigDecimal tip;

    private BigDecimal price;
    private BigDecimal commissionPercentage;
    private BigDecimal commissionAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private Boolean loyaltyDiscountApplied;

    private BigDecimal barberTotal;
    private BigDecimal barbershopRevenue;

}