package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Service;

import java.util.List;

public interface ServiceRepositoryPort extends BaseRepositoryPort<Service> {

    List<Service> findAllActive();

}
