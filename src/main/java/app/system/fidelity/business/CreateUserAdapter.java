package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateUserPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Transactional
public class CreateUserAdapter implements CreateUserPort {

    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoder passwordEncoder;

    @Override
    public User execute(final Context context) {
        User user = context.getData(User.class);

        if (user == null) {
            throw new BusinessException("Por favor, informe os dados do usuário.");
        }

        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new BusinessException("Já existe um usuário com esse email.");
        }

        User newUser = User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser =  userRepositoryPort.save(newUser);

        return savedUser;

    }

}
