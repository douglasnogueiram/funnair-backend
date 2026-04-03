package ai.spring.demo.ai.playground.dto;

import ai.spring.demo.ai.playground.data.BookingDetails;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between BookingDetails and DTOs.
 */
@Component
public class BookingMapper {

    /**
     * Convert BookingDetails to BookingResponseDTO.
     */
    public BookingResponseDTO toResponseDTO(BookingDetails bookingDetails) {
        if (bookingDetails == null) {
            return null;
        }

        return new BookingResponseDTO(
                bookingDetails.bookingNumber(),
                bookingDetails.firstName(),
                bookingDetails.lastName(),
                bookingDetails.date(),
                bookingDetails.bookingStatus(),
                bookingDetails.from(),
                bookingDetails.to(),
                bookingDetails.seatNumber(),
                bookingDetails.bookingClass());
    }
}
