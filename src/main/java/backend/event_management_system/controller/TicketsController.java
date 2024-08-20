package backend.event_management_system.controller;

import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.models.Events;
import backend.event_management_system.models.Tickets;
import backend.event_management_system.models.Users;
import backend.event_management_system.repository.TicketsRepository;
import backend.event_management_system.service.EventsService;
import backend.event_management_system.service.TicketsService;
import backend.event_management_system.service.UsersService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(path = {"/tickets"})
@CrossOrigin(origins = {"http://localhost:3000"})
public class TicketsController {

    private final TicketsRepository ticketsRepository;
    private final TicketsService ticketsService;
    private final EventsService eventsService;
    private final UsersService usersService;

    public TicketsController(TicketsRepository ticketsRepository, TicketsService ticketsService, EventsService eventsService, UsersService usersService) {
        this.ticketsRepository = ticketsRepository;
        this.ticketsService = ticketsService;
        this.eventsService = eventsService;
        this.usersService = usersService;
    }

    @PostMapping("/purchase/{eventId}")
    @PreAuthorize("hasRole('ROLE_BASIC_USER')")
    public Tickets purchaseTicket(@PathVariable Long eventId, @RequestParam int quantity, @RequestParam String ticketType) throws EmailNotFoundException {
        Users user = getCurrentUser();
        Events event = eventsService.getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found."));
        return ticketsService.purchaseTicket(user, event, ticketType, quantity);
    }




    private Users getCurrentUser() throws EmailNotFoundException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails){
            String email = ((UserDetails) principal).getUsername();
            return usersService.findUserByEmail(email);
        } else {
            throw new RuntimeException("User with this email not found.");
        }
    }

}
