package backend.event_management_system.service;

import backend.event_management_system.models.Events;
import backend.event_management_system.models.Tickets;
import backend.event_management_system.models.Users;

import java.util.List;

public interface TicketServiceInterface {

    List<Tickets> getTicketsByUser(Users user);
    Tickets purchaseTicket(Users user, Events event, String ticketType, int quantity, String paymentMethod, String promoCode);
    Tickets confirmPayment(Long ticketId);
    // other methods ...
    List<Tickets> getPendingTickets();
    List<Tickets> getCompletedTickets();

    //for publisher
    List<Tickets> getTicketsByEvent(Events event);
    int countTicketsSoldForEvent(Events event);
    int countTicketsAvailableForEvent(Events event);
    boolean checkTicketAvailability(Events event, int quantityRequested);

    //for admins
    List<Tickets> getAllTickets();
    List<Tickets> getTicketsByUserAndEvent(Users user, Events event);
    void deleteTicket(Long ticketId);
}
