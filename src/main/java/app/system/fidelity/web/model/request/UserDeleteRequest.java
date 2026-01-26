package app.system.fidelity.web.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequest(
        @NotBlank(message = "Email de confirmação é obrigatório")
        @Email(message = "Email inválido")
        @JsonProperty("email_confirmation")
        String emailConfirmation
) {
}