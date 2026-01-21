package app.system.fidelity.persistence.repository;

import app.system.fidelity.domain.Customer;
import app.system.fidelity.persistence.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByEmail(final String email);

    boolean existsByEmail(final String email);

    @Query("SELECT c FROM CustomerEntity c WHERE " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "c.deletedAt IS NULL")
    List<CustomerEntity> searchByNameOrEmailOrPhoneNumber(@Param("searchTerm") String searchTerm);

}
