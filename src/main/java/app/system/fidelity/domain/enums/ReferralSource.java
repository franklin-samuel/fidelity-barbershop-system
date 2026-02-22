package app.system.fidelity.domain.enums;

import lombok.Getter;

@Getter
public enum ReferralSource {
    INDICATION("Indicação"),
    INSTAGRAM("Instagram"),
    GOOGLE("Google"),
    FACEBOOK("Facebook"),
    OUTDOOR("Outdoor/Placa"),
    WALKING("Passando na rua"),
    OTHERS("Outros");

    private final String displayName;

    ReferralSource(final String displayName) {
        this.displayName = displayName;
    }
}