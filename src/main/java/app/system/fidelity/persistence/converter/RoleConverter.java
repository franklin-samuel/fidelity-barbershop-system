package app.system.fidelity.persistence.converter;

import app.system.fidelity.domain.enums.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(final Role role) {
        return role != null ? role.name() : null;
    }

    @Override
    public Role convertToEntityAttribute(final String dbData) {
        if (dbData == null) return null;
        return Role.valueOf(dbData);
    }
}