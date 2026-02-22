package app.system.fidelity.persistence.model;

import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import app.system.fidelity.domain.enums.ReferralSource;
import app.system.fidelity.persistence.converter.GenderConverter;
import app.system.fidelity.persistence.converter.PreferredFrequencyConverter;
import app.system.fidelity.persistence.converter.PreferredStyleConverter;
import app.system.fidelity.persistence.converter.ReferralSourceConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerEntity extends AbstractEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "service_count")
    private Integer serviceCount;

    @Column(name = "discounts_claimed")
    private Integer discountsClaimed;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Convert(converter = GenderConverter.class)
    @Column(name = "gender")
    private Gender gender;

    @Convert(converter = ReferralSourceConverter.class)
    @Column(name = "referral_source")
    private ReferralSource referralSource;

    @Convert(converter = PreferredFrequencyConverter.class)
    @Column(name = "preferred_frequency")
    private PreferredFrequency preferredFrequency;

    @Convert(converter = PreferredStyleConverter.class)
    @Column(name = "preferred_style")
    private PreferredStyle preferredStyle;

    @Column(name = "preferred_barber_id")
    private UUID preferredBarberId;

    @Column(name = "instagram_username", length = 50)
    private String instagramUsername;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "last_visit_date")
    private LocalDateTime lastVisitDate;

    @Column(name = "total_spent", precision = 10, scale = 2)
    private BigDecimal totalSpent;

}