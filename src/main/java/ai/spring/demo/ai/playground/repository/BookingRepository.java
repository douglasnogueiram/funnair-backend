package ai.spring.demo.ai.playground.repository;

import ai.spring.demo.ai.playground.data.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Booking entity.
 * Provides CRUD operations and custom queries for booking management.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    /**
     * Find a booking by booking number, customer first name, and last name.
     * 
     * @param bookingNumber the booking reference number
     * @param firstName     customer's first name (case-insensitive)
     * @param lastName      customer's last name (case-insensitive)
     * @return Optional containing the booking if found
     */
    @Query("SELECT b FROM Booking b JOIN b.customer c WHERE " +
            "LOWER(b.bookingNumber) = LOWER(:bookingNumber) AND " +
            "LOWER(c.firstName) = LOWER(:firstName) AND " +
            "LOWER(c.lastName) = LOWER(:lastName)")
    Optional<Booking> findByBookingNumberAndCustomerName(
            @Param("bookingNumber") String bookingNumber,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);
}
