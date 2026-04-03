package ai.spring.demo.ai.playground.exception;

/**
 * Exception thrown when a booking operation violates business rules.
 */
public class BookingValidationException extends RuntimeException {

    public BookingValidationException(String message) {
        super(message);
    }
}
