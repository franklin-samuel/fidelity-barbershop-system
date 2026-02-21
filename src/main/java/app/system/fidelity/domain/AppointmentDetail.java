package app.system.fidelity.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppointmentDetail extends AbstractDomain {

    private Appointment appointment;
    private String barberName;
    private String customerName;
    private String serviceName;
    private String productName;

}