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

        @Size(min = 10, max = 15, message = "Telefone deve ter entre 10 e 15 caracteres")
        @JsonProperty("phone_number")
        String phoneNumber
) {
}