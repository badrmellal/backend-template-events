package backend.event_management_system.controller;

import backend.event_management_system.dto.TicketsDto;
import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.jwt.JwtTokenProvider;
import backend.event_management_system.models.*;
import backend.event_management_system.service.EventsService;
import backend.event_management_system.service.TicketsService;
import backend.event_management_system.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = {"/tickets"})
@CrossOrigin(origins = {"http://localhost:3000"})
public class TicketsController {

    private final TicketsService ticketsService;
    private final EventsService eventsService;
    private final UsersService usersService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public TicketsController(TicketsService ticketsService, EventsService eventsService, UsersService usersService, JwtTokenProvider jwtTokenProvider) {
        this.ticketsService = ticketsService;
        this.eventsService = eventsService;
        this.usersService = usersService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/purchase/{eventId}")
    @PreAuthorize("hasAuthority('event:read')")
    public ResponseEntity<TicketsDto> purchaseTicket(@RequestHeader("Authorization") String token,
                                                  @PathVariable Long eventId,
                                                  @RequestParam int quantity,
                                                  @RequestParam String ticketTypeName,
                                                  @RequestParam String paymentMethod,
                                                  @RequestParam(required = false) String promoCode) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        Events event = eventsService.getEventById(eventId);
        if (!event.isApproved()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            Tickets ticket = ticketsService.purchaseTicket(user, event, ticketTypeName, quantity, paymentMethod, promoCode);
            TicketsDto ticketsDto = TicketsDto.builder()
                    .isTicketActive(ticket.isTicketActive())
                    .fees(ticket.getFees())
                    .vat(ticket.getVat())
                    .totalAmount(ticket.getTotalAmount())
                    .quantity(ticket.getQuantity())
                    .ticketTypeId(ticket.getId().getTicketTypeId())
                    .build();
            return ResponseEntity.ok(ticketsDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/confirm-payment")
    @PreAuthorize("hasAuthority('event:approve')")
    public ResponseEntity<Tickets> confirmPayment(@RequestParam Long eventId,
                                                  @RequestParam String ticketTypeId,
                                                  @RequestParam String sequenceNumber) {
        TicketId ticketId = new TicketId(eventId, ticketTypeId, sequenceNumber);
        Tickets confirmedTicket = ticketsService.confirmPayment(ticketId);
        return ResponseEntity.ok(confirmedTicket);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('event:read')")
    public ResponseEntity<List<Tickets>> getUserTickets(@RequestHeader("Authorization") String token) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        List<Tickets> userTickets = ticketsService.getTicketsByUser(user);
        return ResponseEntity.ok(userTickets);
    }


    @GetMapping("/all")
    @PreAuthorize("hasAuthority('event:approve')")
    public ResponseEntity<List<Tickets>> getAllTickets() {
        return ResponseEntity.ok(ticketsService.getAllTickets());
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAuthority('event:create')")
    public ResponseEntity<List<Tickets>> getEventTickets(@RequestHeader("Authorization") String token, @PathVariable Long eventId) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        Events event = eventsService.getEventById(eventId);

        if (!user.getEmail().equals(event.getEventManagerUsername())) {
            return ResponseEntity.status(403).build();
        } else {
            List<Tickets> eventTickets = ticketsService.getTicketsByEvent(event);
            return ResponseEntity.ok(eventTickets);
        }
    }

    @GetMapping("/available/{eventId}")
    public ResponseEntity<Integer> totalTicketsAvailableForEvent(@PathVariable Long eventId) {
        Events event = eventsService.getEventById(eventId);
        return ResponseEntity.ok(ticketsService.countTicketsAvailableForEvent(event));
    }

    @GetMapping("/sold/{eventId}")
    public ResponseEntity<Integer> totalTicketsSoldForEvent(@PathVariable Long eventId) {
        Events event = eventsService.getEventById(eventId);
        return ResponseEntity.ok(ticketsService.countTicketsSoldForEvent(event));
    }

    @GetMapping("/check-availability/{eventId}")
    public ResponseEntity<Boolean> checkTicketAvailability(@PathVariable Long eventId,
                                                           @RequestParam String ticketTypeName,
                                                           @RequestParam int quantity) {
        Events event = eventsService.getEventById(eventId);
        boolean isAvailable = ticketsService.checkTicketAvailability(event, ticketTypeName, quantity);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('event:approve')")
    public ResponseEntity<List<Tickets>> getPendingTickets() {
        return ResponseEntity.ok(ticketsService.getPendingTickets());
    }

    @GetMapping("/completed")
    @PreAuthorize("hasAuthority('event:approve')")
    public ResponseEntity<List<Tickets>> getCompletedTickets() {
        return ResponseEntity.ok(ticketsService.getCompletedTickets());
    }

    @GetMapping("/user-event")
    @PreAuthorize("hasAuthority('event:read')")
    public ResponseEntity<List<Tickets>> getTicketsByUserAndEvent(@RequestHeader("Authorization") String token, @RequestParam Long eventId) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        Events event = eventsService.getEventById(eventId);
        List<Tickets> tickets = ticketsService.getTicketsByUserAndEvent(user, event);
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('event:approve')")
    public ResponseEntity<Void> deleteTicket(@RequestParam Long eventId,
                                             @RequestParam String ticketTypeId,
                                             @RequestParam String sequenceNumber) {
        TicketId ticketId = new TicketId(eventId, ticketTypeId, sequenceNumber);
        ticketsService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
}
