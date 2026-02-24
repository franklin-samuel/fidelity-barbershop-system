package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ServiceRepositoryPort extends BaseRepositoryPort<Service> {

    void delete(UUID id);

    List<Service> findAllById(Set<UUID> ids);

}