package app.system.fidelity.web.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerRequest {
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email;

    @NotBlank(message = "Telefone é obrigatório")
    @JsonProperty("phone_number")
    String phoneNumber;
}