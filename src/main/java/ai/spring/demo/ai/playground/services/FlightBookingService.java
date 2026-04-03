package ai.spring.demo.ai.playground.services;

import ai.spring.demo.ai.playground.data.*;
import ai.spring.demo.ai.playground.repository.BookingRepository;
import ai.spring.demo.ai.playground.repository.CustomerRepository;
import ai.spring.demo.ai.playground.repository.PaymentTransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Service
@Validated
@Transactional
public class FlightBookingService {

	// Constants for business rules
	private static final int MIN_HOURS_TO_CHANGE_BOOKING = 24;
	private static final int MIN_HOURS_TO_CANCEL_BOOKING = 48;

	private final BookingRepository bookingRepository;
	private final CustomerRepository customerRepository;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final MeterRegistry meterRegistry;

	@Autowired
	public FlightBookingService(BookingRepository bookingRepository,
			CustomerRepository customerRepository,
			PaymentTransactionRepository paymentTransactionRepository,
			MeterRegistry meterRegistry) {
		this.bookingRepository = bookingRepository;
		this.customerRepository = customerRepository;
		this.paymentTransactionRepository = paymentTransactionRepository;
		this.meterRegistry = meterRegistry;
	}

	public BookingRepository getBookingRepository() {
		return bookingRepository;
	}

	// -----------------------------
	// Booking Service Methods
	// -----------------------------

	/**
	 * Retrieves all bookings from the database.
	 *
	 * @return a list of BookingDetails objects representing all bookings.
	 */
	@Transactional(readOnly = true)
	@org.springframework.cache.annotation.Cacheable("bookings")
	public List<BookingDetails> getBookings() {
		return bookingRepository.findAll().stream()
				.map(this::toBookingDetails)
				.toList();
	}

	/**
	 * Finds a booking by its number and customer name.
	 *
	 * @param bookingNumber the booking number (3-10 digits)
	 * @param firstName     the customer's first name (2-50 characters)
	 * @param lastName      the customer's last name (2-50 characters)
	 * @return the Booking entity if found
	 * @throws IllegalArgumentException if the booking is not found
	 */
	@org.springframework.cache.annotation.Cacheable("bookings")
	public Booking findBooking(
			@NotBlank(message = "Booking number cannot be blank") @Pattern(regexp = "^[0-9]{3,10}$", message = "Booking number must be 3-10 digits") String bookingNumber,
			@NotBlank(message = "First name cannot be blank") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,
			@NotBlank(message = "Last name cannot be blank") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName) {
		return bookingRepository.findByBookingNumberAndCustomerName(bookingNumber, firstName, lastName)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found"));
	}

	/**
	 * Retrieves detailed information about a specific booking.
	 *
	 * @param bookingNumber the booking number
	 * @param firstName     the customer's first name
	 * @param lastName      the customer's last name
	 * @return a BookingDetails object containing the booking information
	 */
	@Transactional(readOnly = true)
	@org.springframework.cache.annotation.Cacheable("bookings")
	public BookingDetails getBookingDetails(
			@NotBlank(message = "Booking number cannot be blank") @Pattern(regexp = "^[0-9]{3,10}$", message = "Booking number must be 3-10 digits") String bookingNumber,
			@NotBlank(message = "First name cannot be blank") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,
			@NotBlank(message = "Last name cannot be blank") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName) {
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			Booking booking = findBooking(bookingNumber, firstName, lastName);
			meterRegistry.counter("booking.operation", "type", "get", "result", "success").increment();
			return toBookingDetails(booking);
		} catch (IllegalArgumentException e) {
			meterRegistry.counter("booking.operation", "type", "get", "result", "not_found").increment();
			throw e;
		} finally {
			sample.stop(Timer.builder("booking.operation.duration").tag("type", "get").register(meterRegistry));
		}
	}

	/**
	 * Changes the date and route of an existing booking.
	 *
	 * @param bookingNumber the booking number
	 * @param firstName     the customer's first name
	 * @param lastName      the customer's last name
	 * @param newDate       the new date for the flight
	 * @param from          the origin airport code
	 * @param to            the destination airport code
	 * @throws IllegalArgumentException if the booking cannot be changed (e.g., too
	 *                                  close to departure)
	 */
	@org.springframework.cache.annotation.CacheEvict(value = "bookings", allEntries = true)
	public void changeBooking(
			@NotBlank(message = "Booking number cannot be blank") @Pattern(regexp = "^[0-9]{3,10}$", message = "Booking number must be 3-10 digits") String bookingNumber,
			@NotBlank(message = "First name cannot be blank") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,
			@NotBlank(message = "Last name cannot be blank") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName,
			@NotBlank(message = "New date cannot be blank") String newDate,
			@NotBlank(message = "Origin cannot be blank") @Size(min = 3, max = 3, message = "Airport code must be 3 characters") String from,
			@NotBlank(message = "Destination cannot be blank") @Size(min = 3, max = 3, message = "Airport code must be 3 characters") String to) {
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			Booking booking = findBooking(bookingNumber, firstName, lastName);

			if (booking.getDate().isBefore(LocalDate.now().plusDays(1))) {
				meterRegistry.counter("booking.operation", "type", "change", "result", "validation_error").increment();
				throw new IllegalArgumentException(
						String.format("Booking cannot be changed within %d hours of the start date.",
								MIN_HOURS_TO_CHANGE_BOOKING));
			}

			booking.setDate(LocalDate.parse(newDate));
			booking.setFrom(from);
			booking.setTo(to);
			bookingRepository.save(booking);
			meterRegistry.counter("booking.operation", "type", "change", "result", "success").increment();
		} catch (IllegalArgumentException e) {
			if (!e.getMessage().contains("hours of the start date")) {
				meterRegistry.counter("booking.operation", "type", "change", "result", "not_found").increment();
			}
			throw e;
		} finally {
			sample.stop(Timer.builder("booking.operation.duration").tag("type", "change").register(meterRegistry));
		}
	}

	/**
	 * Cancels an existing booking.
	 *
	 * @param bookingNumber the booking number
	 * @param firstName     the customer's first name
	 * @param lastName      the customer's last name
	 * @throws IllegalArgumentException if the booking cannot be cancelled (e.g.,
	 *                                  too close to departure)
	 */
	@org.springframework.cache.annotation.CacheEvict(value = "bookings", allEntries = true)
	public void cancelBooking(
			@NotBlank(message = "Booking number cannot be blank") @Pattern(regexp = "^[0-9]{3,10}$", message = "Booking number must be 3-10 digits") String bookingNumber,
			@NotBlank(message = "First name cannot be blank") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,
			@NotBlank(message = "Last name cannot be blank") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName) {
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			Booking booking = findBooking(bookingNumber, firstName, lastName);

			if (booking.getDate().isBefore(LocalDate.now().plusDays(2))) {
				meterRegistry.counter("booking.operation", "type", "cancel", "result", "validation_error").increment();
				throw new IllegalArgumentException(
						String.format("Booking cannot be cancelled within %d hours of the start date.",
								MIN_HOURS_TO_CANCEL_BOOKING));
			}

			booking.setBookingStatus(BookingStatus.CANCELLED);
			bookingRepository.save(booking);
			meterRegistry.counter("booking.operation", "type", "cancel", "result", "success").increment();
		} catch (IllegalArgumentException e) {
			if (!e.getMessage().contains("hours of the start date")) {
				meterRegistry.counter("booking.operation", "type", "cancel", "result", "not_found").increment();
			}
			throw e;
		} finally {
			sample.stop(Timer.builder("booking.operation.duration").tag("type", "cancel").register(meterRegistry));
		}
	}

	private BookingDetails toBookingDetails(Booking booking) {
		return new BookingDetails(
				booking.getBookingNumber(),
				booking.getCustomer().getFirstName(),
				booking.getCustomer().getLastName(),
				booking.getDate(),
				booking.getBookingStatus(),
				booking.getFrom(),
				booking.getTo(),
				booking.getSeatNumber(),
				booking.getBookingClass().toString());
	}

	/**
	 * Changes the seat of an existing booking.
	 *
	 * @param bookingNumber the booking number
	 * @param firstName     the customer's first name
	 * @param lastName      the customer's last name
	 * @param seatNumber    the new seat number
	 */
	@org.springframework.cache.annotation.CacheEvict(value = "bookings", allEntries = true)
	public void changeSeat(
			@NotBlank(message = "Booking number cannot be blank") @Pattern(regexp = "^[0-9]{3,10}$", message = "Booking number must be 3-10 digits") String bookingNumber,
			@NotBlank(message = "First name cannot be blank") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,
			@NotBlank(message = "Last name cannot be blank") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName,
			@NotBlank(message = "Seat number cannot be blank") String seatNumber) {
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			Booking booking = findBooking(bookingNumber, firstName, lastName);
			booking.setSeatNumber(seatNumber);
			bookingRepository.save(booking);
			meterRegistry.counter("booking.operation", "type", "change_seat", "result", "success").increment();
		} catch (IllegalArgumentException e) {
			meterRegistry.counter("booking.operation", "type", "change_seat", "result", "not_found").increment();
			throw e;
		} finally {
			sample.stop(Timer.builder("booking.operation.duration").tag("type", "change_seat").register(meterRegistry));
		}
	}

	// -----------------------------
	// Payment Transaction Helper
	// -----------------------------

	/**
	 * Save a payment transaction to the database.
	 * This method should be called after processing payment with MCP server.
	 * 
	 * @param booking         The booking associated with the transaction
	 * @param transactionType Type of transaction (CANCELLATION, CHANGE, REFUND)
	 * @param paymentId       Payment ID returned from MCP payment server
	 * @param cardId          Card ID used for payment
	 * @param cardLastFour    Last 4 digits of the card
	 * @param amount          Transaction amount
	 * @param status          Payment status (SUCCESS, FAILED, etc.)
	 */
	public void savePaymentTransaction(Booking booking, TransactionType transactionType,
			String paymentId, String cardId, String cardLastFour,
			Double amount, PaymentStatus status) {
		PaymentTransaction transaction = new PaymentTransaction(
				booking, transactionType, paymentId, cardId, cardLastFour, amount, status);
		paymentTransactionRepository.save(transaction);
	}
}
