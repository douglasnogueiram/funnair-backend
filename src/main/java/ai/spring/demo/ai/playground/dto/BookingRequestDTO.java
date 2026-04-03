package ai.spring.demo.ai.playground.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for booking requests.
 * Used for API input validation.
 */
public record BookingRequestDTO(
        @NotBlank(message = "Booking number cannot be blank") @Pattern(regexp = "^[0-9]{3,10}$", message = "Booking number must be 3-10 digits") String bookingNumber,

        @NotBlank(message = "First name cannot be blank") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,

        @NotBlank(message = "Last name cannot be blank") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName) {
}
