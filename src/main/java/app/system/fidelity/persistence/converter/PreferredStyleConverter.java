package app.system.fidelity.persistence.converter;

import app.system.fidelity.domain.enums.PreferredStyle;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PreferredStyleConverter implements AttributeConverter<PreferredStyle, String> {

    @Override
    public String convertToDatabaseColumn(final PreferredStyle preferredStyle) {
        return preferredStyle != null ? preferredStyle.name() : null;
    }

    @Override
    public PreferredStyle convertToEntityAttribute(final String dbData) {
        if (dbData == null) return null;
        return PreferredStyle.valueOf(dbData);
    }
}