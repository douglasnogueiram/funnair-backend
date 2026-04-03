package ai.spring.demo.ai.playground.exception;

/**
 * Exception thrown when a booking is not found.
 */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException(String bookingNumber, String firstName, String lastName) {
        super(String.format("Booking not found for number: %s, customer: %s %s",
                bookingNumber, firstName, lastName));
    }
}
