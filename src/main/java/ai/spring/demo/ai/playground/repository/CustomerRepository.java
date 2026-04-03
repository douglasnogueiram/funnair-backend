package ai.spring.demo.ai.playground.repository;

import ai.spring.demo.ai.playground.data.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity.
 * Provides CRUD operations for customer management.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find a customer by first name and last name.
     * 
     * @param firstName customer's first name
     * @param lastName  customer's last name
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByFirstNameAndLastName(String firstName, String lastName);
}
