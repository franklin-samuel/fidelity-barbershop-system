package app.system.fidelity.web.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CustomerUpdateRequest(
        UUID id,

        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        String name,

        @Email(message = "Email inválido")
        String email,

        @JsonProperty("phone_number")
        String phoneNumber
) {
}