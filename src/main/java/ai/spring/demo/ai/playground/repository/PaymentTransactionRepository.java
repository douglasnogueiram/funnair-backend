package ai.spring.demo.ai.playground.repository;

import ai.spring.demo.ai.playground.data.PaymentTransaction;
import ai.spring.demo.ai.playground.data.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PaymentTransaction entity.
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    /**
     * Find all transactions for a specific booking.
     */
    List<PaymentTransaction> findByBookingBookingNumber(String bookingNumber);

    /**
     * Find all transactions by type.
     */
    List<PaymentTransaction> findByTransactionType(TransactionType transactionType);
}
