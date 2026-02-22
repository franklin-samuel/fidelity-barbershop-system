package app.system.fidelity.domain;

import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import app.system.fidelity.domain.enums.ReferralSource;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Customer extends AbstractDomain {

    private String name;
    private String email;
    private String phoneNumber;
    private Integer serviceCount;
    private Integer discountsClaimed;

    private LocalDate dateOfBirth;
    private Gender gender;
    private String neighborhood;
    private String zipCode;

    private ReferralSource referralSource;
    private PreferredFrequency preferredFrequency;
    private PreferredStyle preferredStyle;
    private UUID preferredBarberId;

    private String instagramUsername;

    private String occupation;

    private LocalDateTime lastVisitDate;
    private BigDecimal totalSpent;

}