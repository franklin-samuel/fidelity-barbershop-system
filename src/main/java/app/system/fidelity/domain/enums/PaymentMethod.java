package app.system.fidelity.domain.enums;

import lombok.Builder;
import lombok.Getter;

@Getter
public enum PaymentMethod {
    CREDIT("Crédito"),
    DEBIT("Débito"),
    PIX("Pix"),
    MONEY("Dinheiro");

    private final String displayName;

    PaymentMethod(final String displayName) {
        this.displayName = displayName;
    }

}
