package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateBarberPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.domain.enums.Role;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@AllArgsConstructor
public class CreateBarberAdapter implements CreateBarberPort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User execute(final Context context) {
        final User form = context.getData(User.class);

        if (form == null) {
            throw new BusinessException("Por favor, informe os dados do barbeiro.");
        }

        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Por favor, informe o nome do barbeiro.");
        }

        if (form.getEmail() == null || form.getEmail().isBlank()) {
            throw new BusinessException("Por favor, informe o email do barbeiro.");
        }

        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BusinessException("Por favor, informe a senha do barbeiro.");
        }

        if (userRepositoryPort.existsByEmail(form.getEmail())) {
            throw new BusinessException("Já existe um usuário com esse email.");
        }

        final User newBarber = User.builder()
                .name(form.getName())
                .email(form.getEmail())
                .password(passwordEncoder.encode(form.getPassword()))
                .role(Role.BARBER)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        return userRepositoryPort.save(newBarber);
    }
}