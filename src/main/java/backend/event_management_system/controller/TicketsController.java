package backend.event_management_system.controller;

import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.jwt.JwtTokenProvider;
import backend.event_management_system.models.Events;
import backend.event_management_system.models.Tickets;
import backend.event_management_system.models.Users;
import backend.event_management_system.repository.TicketsRepository;
import backend.event_management_system.service.EventsService;
import backend.event_management_system.service.TicketsService;
import backend.event_management_system.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Tickets> purchaseTicket(@RequestHeader("Authorization") String token, @PathVariable Long eventId, @RequestParam int quantity, @RequestParam String ticketType)
            throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        Events event = eventsService.getEventById(eventId);
        if (!event.isApproved()){
            return ResponseEntity.badRequest().body(null);
        }
        Tickets ticket = ticketsService.purchaseTicket(user, event, ticketType, quantity);

        return ResponseEntity.ok(ticket);
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
    public List<Tickets> getAllTickets(){
        return ticketsService.getAllTickets();
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAuthority('event:create')")
    public ResponseEntity<List<Tickets>> getEventTickets(@RequestHeader("Authorization") String token, @PathVariable Long eventId) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        Events event = eventsService.getEventById(eventId);

        if (!user.getEmail().equals(event.getEventManagerUsername())){
            return ResponseEntity.status(403).build();
        } else {
            List<Tickets> eventTickets = ticketsService.getTicketsByEvent(event);
            return ResponseEntity.ok(eventTickets);
        }
    }

    @GetMapping("/available/{eventId}")
    public int totalTicketsAvailableForEvent(@PathVariable Long eventId){
        Events event = eventsService.getEventById(eventId);
        return ticketsService.countTicketsAvailableForEvent(event);
    }

    @GetMapping("/sold/{eventId}")
    public int totalTicketsSoldForEvent(@PathVariable Long eventId){
        Events event = eventsService.getEventById(eventId);
        return ticketsService.countTicketsSoldForEvent(event);
    }

    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAuthority('event:approve')")
    public ResponseEntity<Tickets> deleteTicket(@PathVariable Long ticketId){
        ticketsService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }


}
