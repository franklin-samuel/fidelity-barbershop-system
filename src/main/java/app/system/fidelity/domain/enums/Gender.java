package app.system.fidelity.domain.enums;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("Masculino"),
    FEMALE("Feminino"),
    OTHER("Outro"),
    NOT_INFORMED("Não informado");

    private final String displayName;

    Gender(final String displayName) {
        this.displayName = displayName;
    }
}