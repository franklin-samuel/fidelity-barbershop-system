package app.system.fidelity.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "haircuts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class HaircutEntity extends AbstractEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "registered_by", nullable = false)
    private UUID registeredBy;

    @Column(name = "is_free")
    private Boolean isFree;

}