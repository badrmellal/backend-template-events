package backend.event_management_system.service;

import backend.event_management_system.models.Events;
import backend.event_management_system.models.PaymentStatus;
import backend.event_management_system.models.Tickets;
import backend.event_management_system.models.Users;
import backend.event_management_system.repository.EventsRepository;
import backend.event_management_system.repository.TicketsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketsService implements TicketServiceInterface {

    private final TicketsRepository ticketsRepository;
    private final EventsRepository eventsRepository;

    public TicketsService(TicketsRepository ticketsRepository, EventsRepository eventsRepository) {
        this.ticketsRepository = ticketsRepository;
        this.eventsRepository = eventsRepository;
    }

    @Override
    @Transactional
    public Tickets purchaseTicket(Users user, Events event, String ticketType, int quantity, String paymentMethod, String promoCode) {
        if (event.getRemainingTickets() < quantity) {
            throw new RuntimeException("Not enough tickets available for this event.");
        }

        float ticketPrice = event.getEventPrice();
        float subtotal = ticketPrice * quantity;
        float fees = calculateFees(subtotal); // Implement this method based on your fee structure
        float vat = calculateVAT(subtotal + fees); // Implement this method based on your VAT rate
        float totalAmount = subtotal + fees + vat;

        //  promo code if provided
        if (promoCode != null && !promoCode.isEmpty()) {
            totalAmount = applyPromoCode(totalAmount, promoCode);
        }

        Tickets ticket = new Tickets();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setTicketType(ticketType);
        ticket.setTicketPrice(ticketPrice);
        ticket.setQuantity(quantity);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setTicketActive(true);
        ticket.setFees(fees);
        ticket.setVat(vat);
        ticket.setTotalAmount(totalAmount);
        ticket.setPaymentMethod(paymentMethod);
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setPromoCodeUsed(promoCode);

        // Update event's remaining tickets
        event.addTicket(ticket);

        // Save the ticket
        return ticketsRepository.save(ticket);
    }

    @Transactional
    public Tickets confirmPayment(Long ticketId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setPaymentStatus(PaymentStatus.COMPLETED);
        return ticketsRepository.save(ticket);
    }

    private float calculateFees(float subtotal) {
        // Implement your fee calculation logic here
        return subtotal * 0.05f; // Example: 5% fee
    }

    private float calculateVAT(float amount) {
        // Implement your VAT calculation logic here
        return amount * 0.15f; // Example: 15% VAT
    }

    private float applyPromoCode(float totalAmount, String promoCode) {
        // Implement your promo code logic here
        // This is just a placeholder implementation
        if ("DISCOUNT10".equals(promoCode)) {
            return totalAmount * 0.9f; // 10% discount
        }
        return totalAmount;
    }



    @Override
    public void deleteTicket(Long ticketId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (PaymentStatus.COMPLETED.equals(ticket.getPaymentStatus())) {
            throw new RuntimeException("Cannot delete a ticket with completed payment");
        }

        Events event = ticket.getEvent();
        event.removeTicket(ticket);
        ticketsRepository.delete(ticket);
    }

    // You might want to add more methods here, such as:
    public List<Tickets> getPendingTickets() {
        return ticketsRepository.findByPaymentStatus(PaymentStatus.PENDING);
    }

    public List<Tickets> getCompletedTickets() {
        return ticketsRepository.findByPaymentStatus(PaymentStatus.COMPLETED);
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

}
