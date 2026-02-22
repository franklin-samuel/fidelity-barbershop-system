package app.system.fidelity.domain.enums;

import lombok.Getter;

@Getter
public enum PreferredFrequency {
    SEMANAL("Semanal"),
    QUINZENAL("Quinzenal"),
    MENSAL("Mensal"),
    BIMENSAL("A cada 2 meses"),
    TRIMENSAL("A cada 3 meses"),
    NOT_INFORMED("Não informado");

    private final String displayName;

    PreferredFrequency(final String displayName) {
        this.displayName = displayName;
    }
}