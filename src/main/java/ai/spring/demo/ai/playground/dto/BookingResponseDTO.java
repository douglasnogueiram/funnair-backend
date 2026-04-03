package ai.spring.demo.ai.playground.dto;

import ai.spring.demo.ai.playground.data.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

/**
 * DTO for booking responses.
 * Used for API output.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingResponseDTO(
        String bookingNumber,
        String firstName,
        String lastName,
        LocalDate date,
        BookingStatus bookingStatus,
        String from,
        String to,
        String seatNumber,
        String bookingClass) {
}
