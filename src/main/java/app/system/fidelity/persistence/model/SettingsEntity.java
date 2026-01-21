package app.system.fidelity.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "settings")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SettingsEntity extends AbstractEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "haircuts_for_free")
    private Integer haircutsForFree;

}
