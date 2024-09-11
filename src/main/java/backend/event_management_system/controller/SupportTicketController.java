package backend.event_management_system.controller;

import backend.event_management_system.models.SupportTicket;
import backend.event_management_system.models.TicketResponse;
import backend.event_management_system.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "http://localhost:3000")
public class SupportTicketController {
    @Autowired
    private SupportTicketService ticketService;

    @PostMapping("/create-ticket")
    public ResponseEntity<SupportTicket> createTicket(@RequestBody SupportTicket ticket) {
        SupportTicket newTicket = ticketService.createTicket(ticket);
        return new ResponseEntity<>(newTicket, HttpStatus.CREATED);
    }

    @GetMapping("/user/tickets")
    public ResponseEntity<List<SupportTicket>> getUserTickets(@RequestParam String email) {
        List<SupportTicket> tickets = ticketService.getTicketsByUser(email);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<List<SupportTicket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicket> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @PostMapping("/tickets/{id}/responses")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<TicketResponse> addResponse(@PathVariable Long id, @RequestBody TicketResponse response) {
        TicketResponse newResponse = ticketService.addResponse(id, response);
        return new ResponseEntity<>(newResponse, HttpStatus.CREATED);
    }

    @PutMapping("/tickets/{id}/close")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<SupportTicket> closeTicket(@PathVariable Long id) {
        SupportTicket closedTicket = ticketService.closeTicket(id);
        return ResponseEntity.ok(closedTicket);
    }
}
