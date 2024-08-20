package backend.event_management_system.service;

import backend.event_management_system.models.Events;
import backend.event_management_system.models.Tickets;
import backend.event_management_system.models.Users;
import backend.event_management_system.repository.EventsRepository;
import backend.event_management_system.repository.TicketsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

public class TicketsService implements TicketServiceInterface {

    @Autowired
    private TicketsRepository ticketsRepository;

    @Autowired
    private EventsRepository eventsRepository;

    @Override
    @Transactional
    public Tickets purchaseTicket(Users user, Events event, String ticketType, int quantity) {
        if (event.getRemainingTickets() < quantity) {
            throw new RuntimeException("Not enough tickets available for this event.");
        }

        Tickets ticket = new Tickets();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setTicketType(ticketType);
        ticket.setTicketPrice(event.getEventPrice());
        ticket.setQuantity(quantity);
        ticket.setPurchaseDate(LocalDate.now());
        ticket.setTicketActive(true);

        // Update event's remaining tickets
        event.addTicket(ticket);

        // Save the ticket
        return ticketsRepository.save(ticket);
    }

    @Override
    public List<Tickets> getTicketsByUser(Users user) {
        return ticketsRepository.findByUser(user);
    }

    @Override
    public List<Tickets> getTicketsByEvent(Events event) {
        return ticketsRepository.findByEvent(event);
    }

    @Override
    public int countTicketsSoldForEvent(Events event) {
        return ticketsRepository.countByEvent(event);
    }

    @Override
    public int countTicketsAvailableForEvent(Events event) {
        return event.getRemainingTickets();
    }

    @Override
    public boolean checkTicketAvailability(Events event, int quantityRequested) {
        return event.getRemainingTickets() >= quantityRequested;
    }

    @Override
    public List<Tickets> getAllTickets() {
        return ticketsRepository.findAll();
    }

    @Override
    public List<Tickets> getTicketsByUserAndEvent(Users user, Events event) {
        return ticketsRepository.findByUserAndEvent(user, event);
    }

    @Override
    public void deleteTicket(Long ticketId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Events event = ticket.getEvent();
        event.removeTicket(ticket);
        ticketsRepository.delete(ticket);
    }
}
