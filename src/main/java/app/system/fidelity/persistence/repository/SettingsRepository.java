package app.system.fidelity.persistence.repository;

import app.system.fidelity.persistence.model.SettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettingsRepository extends JpaRepository<SettingsEntity, UUID> {
}
