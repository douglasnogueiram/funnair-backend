package ai.spring.demo.ai.playground.api;

import ai.spring.demo.ai.playground.data.BookingDetails;
import ai.spring.demo.ai.playground.data.PaymentTransaction;
import ai.spring.demo.ai.playground.repository.CustomerRepository;
import ai.spring.demo.ai.playground.repository.PaymentTransactionRepository;
import ai.spring.demo.ai.playground.services.CustomerSupportAssistant;
import ai.spring.demo.ai.playground.services.FlightBookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Pattern BOOKING_PATTERN = Pattern.compile("\\b(1\\d{2})\\b");

    private final CustomerSupportAssistant assistant;
    private final FlightBookingService bookingService;
    private final CustomerRepository customerRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    public ChatController(
            CustomerSupportAssistant assistant,
            FlightBookingService bookingService,
            CustomerRepository customerRepository,
            PaymentTransactionRepository transactionRepository,
            ObjectMapper objectMapper) {
        this.assistant = assistant;
        this.bookingService = bookingService;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    // -----------------------------------------------------------------------
    // POST /api/chat/message — SSE stream
    // -----------------------------------------------------------------------

    @PostMapping("/message")
    public SseEmitter chat(@RequestBody ChatRequest request) {
        var emitter = new SseEmitter(120_000L);

        Matcher m = BOOKING_PATTERN.matcher(request.message() != null ? request.message() : "");
        final String bookingNumber = m.find() ? m.group(1) : null;

        // Run on a virtual/cached thread so we don't block reactive scheduler
        var executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                assistant.chat(request.chatId(), request.message())
                        .subscribe(
                                token -> send(emitter, Map.of("type", "token", "content", token)),
                                error -> {
                                    send(emitter, Map.of("type", "error", "message", error.getMessage()));
                                    emitter.complete();
                                },
                                () -> {
                                    var done = bookingNumber != null
                                            ? Map.of("type", "done", "bookingNumber", bookingNumber)
                                            : Map.of("type", "done");
                                    send(emitter, done);
                                    emitter.complete();
                                });
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });

        return emitter;
    }

    // -----------------------------------------------------------------------
    // GET /api/chat/bookings
    // -----------------------------------------------------------------------

    @GetMapping("/bookings")
    public List<BookingDetails> getBookings() {
        return bookingService.getBookings();
    }

    // -----------------------------------------------------------------------
    // GET /api/chat/customers
    // -----------------------------------------------------------------------

    @GetMapping("/customers")
    public List<CustomerDTO> getCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> new CustomerDTO(c.getId(), c.getFirstName(), c.getLastName()))
                .toList();
    }

    // -----------------------------------------------------------------------
    // GET /api/chat/transactions/{bookingNumber}
    // -----------------------------------------------------------------------

    @GetMapping("/transactions/{bookingNumber}")
    public List<PaymentTransactionDTO> getTransactions(@PathVariable String bookingNumber) {
        return transactionRepository.findByBookingBookingNumber(bookingNumber).stream()
                .map(this::toDTO)
                .toList();
    }

    // -----------------------------------------------------------------------
    // PATCH /api/chat/bookings/{bookingNumber}/seat
    // -----------------------------------------------------------------------

    @PatchMapping("/bookings/{bookingNumber}/seat")
    public void changeSeat(@PathVariable String bookingNumber, @RequestBody SeatChangeRequest request) {
        bookingService.changeSeat(bookingNumber, request.firstName(), request.lastName(), request.seatNumber());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void send(SseEmitter emitter, Map<String, ?> data) {
        try {
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(data)));
        } catch (IOException e) {
            // Client disconnected — ignore
        }
    }

    private PaymentTransactionDTO toDTO(PaymentTransaction tx) {
        return new PaymentTransactionDTO(
                tx.getId(),
                tx.getTransactionType() != null ? tx.getTransactionType().name() : null,
                tx.getPaymentId(),
                tx.getCardLastFour(),
                tx.getAmount(),
                tx.getPaymentStatus() != null ? tx.getPaymentStatus().name() : null,
                tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : null);
    }

    // -----------------------------------------------------------------------
    // Record types
    // -----------------------------------------------------------------------

    public record ChatRequest(String chatId, String message) {}
    public record SeatChangeRequest(String firstName, String lastName, String seatNumber) {}
    public record CustomerDTO(Long id, String firstName, String lastName) {}
    public record PaymentTransactionDTO(
            Long id,
            String transactionType,
            String paymentId,
            String cardLastFour,
            Double amount,
            String paymentStatus,
            String transactionDate) {}
}
