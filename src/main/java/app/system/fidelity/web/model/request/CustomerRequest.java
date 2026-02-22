package app.system.fidelity.web.model.request;

import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import app.system.fidelity.domain.enums.ReferralSource;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    private Gender gender;

    @JsonProperty("referral_source")
    private ReferralSource referralSource;

    @JsonProperty("preferred_frequency")
    private PreferredFrequency preferredFrequency;

    @JsonProperty("preferred_style")
    private PreferredStyle preferredStyle;

    @JsonProperty("preferred_barber_id")
    private UUID preferredBarberId;

    @JsonProperty("instagram_username")
    private String instagramUsername;

    private String occupation;
}