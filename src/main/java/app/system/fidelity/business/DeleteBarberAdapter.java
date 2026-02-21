package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteBarberPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.domain.enums.Role;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class DeleteBarberAdapter implements DeleteBarberPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public Void execute(final Context context) {
        final UUID barberId = context.getProperty("barberId", UUID.class);
        final UUID authenticatedUserId = context.getProperty("authenticatedUserId", UUID.class);
        final String emailConfirmation = context.getProperty("emailConfirmation", String.class);

        if (barberId == null) {
            throw new BusinessException("Barbeiro não encontrado.");
        }

        if (authenticatedUserId == null) {
            throw new BusinessException("Usuário não autenticado.");
        }

        if (emailConfirmation == null || emailConfirmation.isBlank()) {
            throw new BusinessException("Por favor, confirme o email do barbeiro.");
        }

        final User barber = userRepositoryPort.get(barberId)
                .orElseThrow(() -> new BusinessException("Barbeiro não encontrado."));

        if (barber.getRole() != Role.BARBER) {
            throw new BusinessException("O usuário informado não é um barbeiro.");
        }

        if (!barber.getEmail().equalsIgnoreCase(emailConfirmation.trim())) {
            throw new BusinessException("O email de confirmação não corresponde ao email do barbeiro.");
        }

        barber.setDeletedAt(LocalDateTime.now());
        userRepositoryPort.save(barber);

        return null;
    }
}