package ai.spring.demo.ai.playground.services;

import ai.spring.demo.ai.playground.data.BookingDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings-tools")
@Validated
public class BookingToolsController {

    private final BookingTools bookingTools;
    private static final Logger logger = LoggerFactory.getLogger(BookingToolsController.class);

    @Autowired
    public BookingToolsController(BookingTools bookingTools) {
        this.bookingTools = bookingTools;
    }

    // -------------------------
    // 1. GET /bookings - Get booking details
    // -------------------------
    @GetMapping("/bookings")
    public ResponseEntity<BookingDetails> getBooking(
            @RequestParam @NotBlank String bookingNumber,
            @RequestParam @NotBlank String firstName,
            @RequestParam @NotBlank String lastName) {

        logger.info("GET /bookings - Retrieving booking: {}", bookingNumber);

        try {
            BookingDetails booking = bookingTools.getBookingDetails(bookingNumber, firstName, lastName);

            if (booking == null || booking.bookingStatus() == null) {
                logger.warn("Booking not found: {}", bookingNumber);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            logger.error("Error retrieving booking {}: {}", bookingNumber, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // -------------------------
    // 2. PUT /bookings - Update booking
    // -------------------------
    @PutMapping("/bookings")
    public ResponseEntity<Void> updateBooking(@RequestBody @Valid ChangeBookingRequest req) {
        logger.info("PUT /bookings - Updating booking: {}", req.bookingNumber());

        try {
            bookingTools.changeBooking(
                    req.bookingNumber(),
                    req.firstName(),
                    req.lastName(),
                    req.newDate(),
                    req.from(),
                    req.to());

            logger.info("Booking {} updated successfully", req.bookingNumber());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid booking update request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating booking {}: {}", req.bookingNumber(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public record ChangeBookingRequest(
            @NotBlank String bookingNumber,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String newDate,
            @NotBlank String from,
            @NotBlank String to) {
    }

    // -------------------------
    // 3. DELETE /bookings - Cancel booking
    // -------------------------
    @DeleteMapping("/bookings")
    public ResponseEntity<Void> cancelBooking(@RequestBody @Valid CancelBookingRequest req) {
        logger.info("DELETE /bookings - Canceling booking: {}", req.bookingNumber());

        try {
            bookingTools.cancelBooking(req.bookingNumber(), req.firstName(), req.lastName());
            logger.info("Booking {} canceled successfully", req.bookingNumber());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid booking cancellation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error canceling booking {}: {}", req.bookingNumber(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public record CancelBookingRequest(
            @NotBlank String bookingNumber,
            @NotBlank String firstName,
            @NotBlank String lastName) {
    }

    // -------------------------
    // Utility Endpoints
    // -------------------------

    @GetMapping("/utils/current-datetime")
    public ResponseEntity<DateTimeResponse> getCurrentDateTime() {
        logger.debug("GET /utils/current-datetime");
        return ResponseEntity.ok(new DateTimeResponse(bookingTools.getCurrentDateTime()));
    }

    public record DateTimeResponse(String dateTime) {
    }

    @GetMapping("/utils/sum-integers")
    public ResponseEntity<CalculationResponse> sumTwoIntegers(
            @RequestParam int numberA,
            @RequestParam int numberB) {
        logger.debug("GET /utils/sum-integers - {} + {}", numberA, numberB);
        int result = bookingTools.sum(numberA, numberB);
        return ResponseEntity.ok(new CalculationResponse(result));
    }

    @GetMapping("/utils/sum-decimals")
    public ResponseEntity<DecimalCalculationResponse> sumTwoDecimals(
            @RequestParam double numberA,
            @RequestParam double numberB) {
        logger.debug("GET /utils/sum-decimals - {} + {}", numberA, numberB);
        double result = bookingTools.sumDecimals(numberA, numberB);
        return ResponseEntity.ok(new DecimalCalculationResponse(result));
    }

    @GetMapping("/utils/subtract")
    public ResponseEntity<CalculationResponse> subtractTwoNumbers(
            @RequestParam int numberA,
            @RequestParam int numberB) {
        logger.debug("GET /utils/subtract - {} - {}", numberA, numberB);
        int result = bookingTools.subtract(numberA, numberB);
        return ResponseEntity.ok(new CalculationResponse(result));
    }

    public record CalculationResponse(int result) {
    }

    public record DecimalCalculationResponse(double result) {
    }
}
