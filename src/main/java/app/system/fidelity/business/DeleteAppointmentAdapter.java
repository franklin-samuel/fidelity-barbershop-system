package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteAppointmentPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteAppointmentAdapter implements DeleteAppointmentPort {

    private final AppointmentRepositoryPort repository;

    @Override
    public Void execute(final Context context) {

        final UUID appointmentId = context.getData(UUID.class);

        if (appointmentId == null) {
            throw new BusinessException("Atendimento não encontrado");
        }

        repository.get(appointmentId)
                .orElseThrow(() -> new BusinessException("Atendimento não encontrado"));

        repository.delete(appointmentId);

        return null;
    }
}
