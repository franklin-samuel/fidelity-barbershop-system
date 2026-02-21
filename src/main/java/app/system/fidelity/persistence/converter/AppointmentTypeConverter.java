package app.system.fidelity.persistence.converter;

import app.system.fidelity.domain.enums.AppointmentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AppointmentTypeConverter implements AttributeConverter<AppointmentType, String> {

    @Override
    public String convertToDatabaseColumn(final AppointmentType appointmentType) {
        return appointmentType != null ? appointmentType.name() : null;
    }

    @Override
    public AppointmentType convertToEntityAttribute(final String dbData) {
        if (dbData == null) return null;
        return AppointmentType.valueOf(dbData);
    }
}