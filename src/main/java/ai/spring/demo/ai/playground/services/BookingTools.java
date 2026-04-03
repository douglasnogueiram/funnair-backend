package ai.spring.demo.ai.playground.services;

import ai.spring.demo.ai.playground.data.BookingDetails;

import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

@Service
public class BookingTools {

	private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

	private final FlightBookingService flightBookingService;

	@Autowired
	public BookingTools(FlightBookingService flightBookingService) {
		this.flightBookingService = flightBookingService;
	}

	/**
	 * Tool to retrieve booking details.
	 *
	 * @param bookingNumber the booking number
	 * @param firstName     the customer's first name
	 * @param lastName      the customer's last name
	 * @return the booking details or a default object if not found
	 */
	@Tool(name = "get-booking", description = "Get booking details")
	public BookingDetails getBookingDetails(String bookingNumber, String firstName, String lastName) {
		MDC.put("bookingRef", bookingNumber);
		MDC.put("operationType", "get");
		logger.info("🔍 [TOOL] get-booking called - Booking: {}, Name: {} {}", bookingNumber, firstName, lastName);
		try {
			BookingDetails result = flightBookingService.getBookingDetails(bookingNumber, firstName, lastName);
			String status = result != null && result.bookingStatus() != null ? result.bookingStatus().toString() : "NOT_FOUND";
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "get");
			Span.current().setAttribute("booking.result", status);
			logger.info("✅ [TOOL] get-booking completed - Status: {}", status);
			return result;
		} catch (Exception e) {
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "get");
			Span.current().setAttribute("booking.result", "error");
			logger.warn("⚠️ [TOOL] get-booking failed - {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
			return new BookingDetails(bookingNumber, firstName, lastName, null, null,
					null, null, null, null);
		} finally {
			MDC.remove("bookingRef");
			MDC.remove("operationType");
		}
	}

	/**
	 * Tool to change booking dates and route.
	 *
	 * @param bookingNumber the booking number
	 * @param firstName     the customer's first name
	 * @param lastName      the customer's last name
	 * @param newDate       the new date
	 * @param from          the origin airport
	 * @param to            the destination airport
	 */
	@Tool(name = "change-booking", description = "Change booking dates")
	public void changeBooking(String bookingNumber, String firstName, String lastName, String newDate, String from,
			String to) {
		MDC.put("bookingRef", bookingNumber);
		MDC.put("operationType", "change");
		logger.info("✏️ [TOOL] change-booking called - Booking: {}, New Date: {}, Route: {} -> {}",
				bookingNumber, newDate, from, to);
		try {
			flightBookingService.changeBooking(bookingNumber, firstName, lastName, newDate, from, to);
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "change");
			Span.current().setAttribute("booking.result", "success");
			logger.info("✅ [TOOL] change-booking completed - Booking {} updated", bookingNumber);
		} catch (Exception e) {
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "change");
			Span.current().setAttribute("booking.result", "error");
			throw e;
		} finally {
			MDC.remove("bookingRef");
			MDC.remove("operationType");
		}
	}

	/**
	 * Tool to cancel a booking.
	 *
	 * @param bookingNumber the booking number
	 * @param firstName     the customer's first name
	 * @param lastName      the customer's last name
	 */
	@Tool(name = "cancel-booking", description = "Cancel booking")
	public void cancelBooking(String bookingNumber, String firstName, String lastName) {
		MDC.put("bookingRef", bookingNumber);
		MDC.put("operationType", "cancel");
		logger.info("❌ [TOOL] cancel-booking called - Booking: {}, Name: {} {}", bookingNumber, firstName, lastName);
		try {
			flightBookingService.cancelBooking(bookingNumber, firstName, lastName);
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "cancel");
			Span.current().setAttribute("booking.result", "success");
			logger.info("✅ [TOOL] cancel-booking completed - Booking {} canceled", bookingNumber);
		} catch (Exception e) {
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "cancel");
			Span.current().setAttribute("booking.result", "error");
			throw e;
		} finally {
			MDC.remove("bookingRef");
			MDC.remove("operationType");
		}
	}

	/**
	 * Tool to get the current date and time.
	 *
	 * @return the current date and time formatted as a string
	 */
	@Tool(name = "current-date-time", description = "Current date and time")
	public String getCurrentDateTime() {
		String currentDateTime = java.time.LocalDateTime.now()
				.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		logger.info("🕐 [TOOL] current-date-time called - Result: {}", currentDateTime);
		return currentDateTime;
	}

	/**
	 * Tool to sum two integers.
	 *
	 * @param numberA the first number
	 * @param numberB the second number
	 * @return the sum
	 */
	@Tool(name = "sum-two-integers", description = "sum of two integer numbers")
	public int sum(int numberA, int numberB) {
		int result = numberA + numberB;
		logger.info("➕ [TOOL] sum-two-integers called - {} + {} = {}", numberA, numberB, result);
		return result;
	}

	/**
	 * Tool to sum two decimal numbers.
	 *
	 * @param numberA the first number
	 * @param numberB the second number
	 * @return the sum
	 */
	@Tool(name = "sum-two-decimal", description = "sum of two numbers with decimal places")
	public double sumDecimals(double numberA, double numberB) {
		double result = numberA + numberB;
		logger.info("➕ [TOOL] sum-two-decimal called - {} + {} = {}", numberA, numberB, result);
		return result;
	}

	/**
	 * Tool to subtract two numbers.
	 *
	 * @param numberA the first number
	 * @param numberB the second number
	 * @return the difference
	 */
	@Tool(name = "difference-two-numbers", description = "difference of two numbers")
	public int subtract(int numberA, int numberB) {
		int result = numberA - numberB;
		logger.info("➖ [TOOL] difference-two-numbers called - {} - {} = {}", numberA, numberB, result);
		return result;
	}

	/**
	 * Tool to log a payment transaction.
	 *
	 * @param bookingNumber   the booking number
	 * @param transactionType the transaction type (CANCELLATION, CHANGE, REFUND)
	 * @param paymentId       the payment ID from MCP
	 * @param cardId          the card ID
	 * @param cardLastFour    the last 4 digits of the card
	 * @param amount          the amount
	 * @param success         whether the payment was successful
	 * @return a confirmation message
	 */
	@Tool(name = "change-seat", description = "Change the seat number of an existing booking. "
			+ "Valid seats: rows 1 to 30, columns A, B, C, D, E, F (e.g. '5B', '30F'). "
			+ "Does not require payment. Call this when the user requests a seat change.")
	public String changeSeat(String bookingNumber, String firstName, String lastName, String newSeatNumber) {
		MDC.put("bookingRef", bookingNumber);
		MDC.put("operationType", "change_seat");
		logger.info("💺 [TOOL] change-seat called - Booking: {}, Name: {} {}, New Seat: {}",
				bookingNumber, firstName, lastName, newSeatNumber);
		try {
			flightBookingService.changeSeat(bookingNumber, firstName, lastName, newSeatNumber);
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "change_seat");
			Span.current().setAttribute("booking.result", "success");
			logger.info("✅ [TOOL] change-seat completed - Booking {} seat changed to {}", bookingNumber, newSeatNumber);
			return String.format("Assento alterado com sucesso para %s na reserva %s.", newSeatNumber, bookingNumber);
		} catch (Exception e) {
			Span.current().setAttribute("booking.reference", bookingNumber);
			Span.current().setAttribute("booking.operation", "change_seat");
			Span.current().setAttribute("booking.result", "error");
			logger.warn("⚠️ [TOOL] change-seat failed - {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
			throw e;
		} finally {
			MDC.remove("bookingRef");
			MDC.remove("operationType");
		}
	}

	@Tool(name = "log-payment-transaction", description = "Log a payment transaction after processing with MCP payment server. "
			+
			"Call this immediately after receiving payment response from MCP payment server to record the transaction.")
	public String logPaymentTransaction(
			String bookingNumber,
			String transactionType,
			String paymentId,
			String cardId,
			String cardLastFour,
			Double amount,
			Boolean success) {

		logger.warn("🚨🚨🚨 LOG-PAYMENT-TRANSACTION TOOL CALLED! 🚨🚨🚨");
		logger.info(
				"💳 [TOOL] log-payment-transaction called - Booking: {}, Type: {}, PaymentId: {}, Amount: {}, Success: {}",
				bookingNumber, transactionType, paymentId, amount, success);

		try {
			// Find the booking by number only
			ai.spring.demo.ai.playground.data.Booking booking = flightBookingService.getBookingRepository()
					.findById(bookingNumber)
					.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingNumber));

			// Determine transaction type
			ai.spring.demo.ai.playground.data.TransactionType type = ai.spring.demo.ai.playground.data.TransactionType
					.valueOf(transactionType.toUpperCase());

			// Determine payment status
			ai.spring.demo.ai.playground.data.PaymentStatus status = success
					? ai.spring.demo.ai.playground.data.PaymentStatus.SUCCESS
					: ai.spring.demo.ai.playground.data.PaymentStatus.FAILED;

			// Save the transaction
			flightBookingService.savePaymentTransaction(
					booking, type, paymentId, cardId, cardLastFour, amount, status);

			logger.info("✅ [TOOL] log-payment-transaction completed - Transaction saved for booking {}", bookingNumber);
			return String.format("Payment transaction logged: %s for booking %s (Amount: %.2f, Status: %s)",
					transactionType, bookingNumber, amount, status);

		} catch (Exception e) {
			logger.error("❌ [TOOL] log-payment-transaction failed - Booking: {}, Error: {}",
					bookingNumber, e.getMessage());
			return "Failed to log payment transaction: " + e.getMessage();
		}
	}

}
