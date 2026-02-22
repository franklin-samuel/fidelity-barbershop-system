package app.system.fidelity.persistence.converter;

import app.system.fidelity.domain.enums.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(final Gender gender) {
        return gender != null ? gender.name() : null;
    }

    @Override
    public Gender convertToEntityAttribute(final String dbData) {
        if (dbData == null) return null;
        return Gender.valueOf(dbData);
    }
}