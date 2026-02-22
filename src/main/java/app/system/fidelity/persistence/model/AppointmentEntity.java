package app.system.fidelity.persistence.model;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import app.system.fidelity.persistence.converter.AppointmentTypeConverter;
import app.system.fidelity.persistence.converter.PaymentMethodConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class AppointmentEntity extends AbstractEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "barber_id", nullable = false)
    private UUID barberId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Convert(converter = AppointmentTypeConverter.class)
    @Column(name = "type", nullable = false)
    private AppointmentType type;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "product_id")
    private UUID productId;

    @Convert(converter = PaymentMethodConverter.class)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "tip", precision = 10, scale = 2)
    private BigDecimal tip;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "commission_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "loyalty_discount_applied", nullable = false)
    private Boolean loyaltyDiscountApplied;

    @Column(name = "barber_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal barberTotal;

    @Column(name = "barbershop_revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal barbershopRevenue;
}