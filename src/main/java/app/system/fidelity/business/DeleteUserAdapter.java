package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteUserPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class DeleteUserAdapter implements DeleteUserPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public Void execute(final Context context) {

        final UUID userId = context.getProperty("userId", UUID.class);
        final UUID authenticatedUserId = context.getProperty("authenticatedUserId", UUID.class);
        final String emailConfirmation = context.getProperty("emailConfirmation", String.class);

        if (userId == null) {
            throw new BusinessException("Usuário não encontrado");
        }

        if (authenticatedUserId == null) {
            throw new BusinessException("Usuário não autenticado.");
        }

        if (emailConfirmation == null || emailConfirmation.trim().isEmpty()) {
            throw new BusinessException("Por favor, confirme o email do usuário.");
        }

        if (userId.equals(authenticatedUserId)) {
            throw new BusinessException("Você não pode deletar sua própria conta.");
        }

        User user = userRepositoryPort.get(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        if (!user.getEmail().equalsIgnoreCase(emailConfirmation.trim())) {
            throw new BusinessException("O email de confirmação não corresponde ao email do usuário.");
        }

        user.setDeletedAt(LocalDateTime.now());
        userRepositoryPort.save(user);

        return null;

    }

}
