package backend.event_management_system.service;

import backend.event_management_system.exceptions.ResourceNotFoundException;
import backend.event_management_system.models.SupportTicket;
import backend.event_management_system.models.TicketResponse;
import backend.event_management_system.repository.SupportTicketRepository;
import backend.event_management_system.repository.TicketResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketService {
    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private TicketResponseRepository responseRepository;

    public SupportTicket createTicket(SupportTicket ticket) {
        ticket.setStatus("Open");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public SupportTicket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    public List<SupportTicket> getTicketsByUser(String email) {
        return ticketRepository.findByEmail(email);
    }

    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<SupportTicket> getOpenTickets() {
        return ticketRepository.findByStatus("Open");
    }

    public TicketResponse addResponse(Long ticketId, TicketResponse response) {
        SupportTicket ticket = getTicket(ticketId);
        response.setTicket(ticket);
        response.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setStatus("In Progress");
        ticketRepository.save(ticket);
        return responseRepository.save(response);
    }

    public SupportTicket closeTicket(Long ticketId) {
        SupportTicket ticket = getTicket(ticketId);
        ticket.setStatus("Closed");
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }
}
