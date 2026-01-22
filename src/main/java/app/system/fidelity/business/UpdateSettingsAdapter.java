package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.UpdateSettingsPort;
import app.system.fidelity.core.persistence.SettingsRepositoryPort;
import app.system.fidelity.domain.Settings;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Transactional
public class UpdateSettingsAdapter implements UpdateSettingsPort {

    private final SettingsRepositoryPort repository;

    @Override
    public Settings execute(final Context context) {

        Settings settingsForm = context.getData(Settings.class);

        if (settingsForm == null) {
            throw new BusinessException("Por favor, insira o número de cortes.");
        }

        if (settingsForm.getHaircutsForFree() == null || settingsForm.getHaircutsForFree() <= 0) {
            throw new BusinessException("O número de cortes para ganhar um grátis deve ser maior que zero");
        }

        Settings settings = repository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações não encontradas"));

        settings.setHaircutsForFree(settingsForm.getHaircutsForFree());
        settings.setUpdatedAt(LocalDateTime.now());

        return repository.save(settings);
    }

}
