package app.system.fidelity.core.persistence.commons;

public interface WriteRepositoryPort<T> {
    T save(final T model);
}
