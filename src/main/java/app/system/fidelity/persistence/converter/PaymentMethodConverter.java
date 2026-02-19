package app.system.fidelity.persistence.converter;

import app.system.fidelity.domain.enums.PaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, String> {

    @Override
    public String convertToDatabaseColumn(final PaymentMethod paymentMethod) {
        return paymentMethod != null ? paymentMethod.name() : null;
    }

    @Override
    public PaymentMethod convertToEntityAttribute(final String dbData) {
        if (dbData == null) return null;
        return PaymentMethod.valueOf(dbData);
    }
}