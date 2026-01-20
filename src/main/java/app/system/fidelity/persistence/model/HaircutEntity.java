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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_haircut_customer"))
    private CustomerEntity customer_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by", foreignKey = @ForeignKey(name = "fk_haircut_user"))
    private UserEntity registered_by;

    @Column(name = "is_free")
    private Boolean is_free;

}
