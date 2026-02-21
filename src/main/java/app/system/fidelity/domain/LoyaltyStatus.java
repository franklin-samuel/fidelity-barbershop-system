package app.system.fidelity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyStatus {

    private boolean hasDiscount;
    private Integer serviceCount;
    private Integer discountsClaimed;

    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

}