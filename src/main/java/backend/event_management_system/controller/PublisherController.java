package backend.event_management_system.controller;

import backend.event_management_system.dto.EventsDto;
import backend.event_management_system.dto.TicketsDto;
import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.models.Events;
import backend.event_management_system.models.Tickets;
import backend.event_management_system.models.Users;
import backend.event_management_system.service.EventsService;
import backend.event_management_system.service.TicketsService;
import backend.event_management_system.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(path = {"/publisher"})
@Controller
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class PublisherController {

    private final TicketsService ticketsService;
    private final EventsService eventsService;
    private final UsersService usersService;

    @GetMapping("tickets-details/{publisherEmail}")
    @PreAuthorize("hasAuthority('event:create')")
    public ResponseEntity<List<TicketsDto>> getTicketsDetails(@PathVariable String publisherEmail) throws EmailNotFoundException {
        List<EventsDto> events = eventsService.getEventsByUsername(publisherEmail, false);
        List<TicketsDto> tickets = ticketsService.getTicketsByEvents(events);

        return ResponseEntity.ok(tickets);
    }
}
