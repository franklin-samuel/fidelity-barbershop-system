package app.system.fidelity.persistence.converter;

import app.system.fidelity.domain.enums.ReferralSource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReferralSourceConverter implements AttributeConverter<ReferralSource, String> {

    @Override
    public String convertToDatabaseColumn(final ReferralSource referralSource) {
        return referralSource != null ? referralSource.name() : null;
    }

    @Override
    public ReferralSource convertToEntityAttribute(final String dbData) {
        if (dbData == null) return null;
        return ReferralSource.valueOf(dbData);
    }
}