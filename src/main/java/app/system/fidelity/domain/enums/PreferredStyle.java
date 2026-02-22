package app.system.fidelity.domain.enums;

import lombok.Getter;

@Getter
public enum PreferredStyle {
    LOW_FADE("Low Fade"),
    MEDIUM_FADE("Degradê"),
    HIGH_FADE("High Fade/Militar"),
    TAPER_FADE("Taper Fade/Americano"),
    BALD("Raspado"),
    SOCIAL("Social"),
    CLASSIC("Clássico"),
    OTHERS("Outros"),
    NOT_INFORMED("Não informado");

    private final String displayName;

    PreferredStyle(final String displayName) {
        this.displayName = displayName;
    }
}