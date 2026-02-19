package app.system.fidelity.persistence.mapper;

import app.system.fidelity.domain.Appointment;
import app.system.fidelity.persistence.model.AppointmentEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AppointmentMapper {

    Appointment map(final AppointmentEntity source);

    AppointmentEntity map(final Appointment source);

}