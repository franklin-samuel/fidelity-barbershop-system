package app.system.fidelity.persistence.model;

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

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

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
}