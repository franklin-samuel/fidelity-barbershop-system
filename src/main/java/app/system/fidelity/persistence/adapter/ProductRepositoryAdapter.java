package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.ProductRepositoryPort;
import app.system.fidelity.domain.Product;
import app.system.fidelity.persistence.mapper.ProductMapper;
import app.system.fidelity.persistence.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.of;

@Repository
@Transactional
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public Optional<Product> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Product save(final Product model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save product"));
    }

    @Override
    public List<Product> findAll() {
        return of(repository.findAll())
                .orElse(new ArrayList<>())
                .stream().map(mapper::map).toList();
    }

    @Override
    public List<Product> findAllActive() {
        return repository.findByActiveTrue().stream().map(mapper::map).toList();
    }
}