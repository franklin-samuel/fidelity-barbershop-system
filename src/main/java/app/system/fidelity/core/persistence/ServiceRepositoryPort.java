package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Service;

import java.util.List;
import java.util.UUID;

public interface ServiceRepositoryPort extends BaseRepositoryPort<Service> {

    List<Service> findAllActive();

    void delete(UUID id);

}
