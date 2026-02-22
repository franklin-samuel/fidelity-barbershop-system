package app.system.fidelity.persistence.converter;

import app.system.fidelity.domain.enums.PreferredFrequency;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PreferredFrequencyConverter implements AttributeConverter<PreferredFrequency, String> {

    @Override
    public String convertToDatabaseColumn(final PreferredFrequency preferredFrequency) {
        return preferredFrequency != null ? preferredFrequency.name() : null;
    }

    @Override
    public PreferredFrequency convertToEntityAttribute(final String dbData) {
        if (dbData == null) return null;
        return PreferredFrequency.valueOf(dbData);
    }
}