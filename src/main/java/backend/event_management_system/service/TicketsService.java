package backend.event_management_system.service;

import backend.event_management_system.constant.TicketSequenceGenerator;
import backend.event_management_system.dto.TicketsDto;
import backend.event_management_system.models.*;
import backend.event_management_system.repository.EventsRepository;
import backend.event_management_system.repository.TicketsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static backend.event_management_system.constant.TicketSequenceGenerator.generateSequenceNumber;

@Service
@RequiredArgsConstructor
public class TicketsService implements TicketServiceInterface {

    private final TicketsRepository ticketsRepository;


    @Override
    @Transactional
    public Tickets purchaseTicket(Users user, Events event, String ticketTypeName, int quantity, String paymentMethod, String promoCode) {
        EventTicketTypes ticketType = event.getTicketTypes().stream()
                .filter(tt -> tt.getName().equals(ticketTypeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ticket type not found for this event."));

        if (ticketType.getRemainingTickets() < quantity) {
            throw new RuntimeException("Not enough tickets available for this ticket type.");
        }

        float ticketPrice = ticketType.getPrice();
        float subtotal = ticketPrice * quantity;
        float fees = calculateFees(subtotal);
        float vat = calculateVAT(subtotal + fees);
        float totalAmount = subtotal + fees + vat;

        if (promoCode != null && !promoCode.isEmpty()) {
            totalAmount = applyPromoCode(totalAmount, promoCode);
        }

        String sequenceNumber = TicketSequenceGenerator.generateSequenceNumber();
        TicketId ticketId = new TicketId(event.getId(), ticketType.getTicketTypeId(), sequenceNumber);
        Tickets ticket = new Tickets();
        ticket.setId(ticketId);
        ticket.setEvent(event);
        ticket.setUser(user);
        ticket.setQuantity(quantity);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setTicketActive(true);
        ticket.setFees(fees);
        ticket.setVat(vat);
        ticket.setTotalAmount(totalAmount);
        ticket.setPaymentMethod(paymentMethod);
        ticket.setPaymentStatus(PaymentStatus.PENDING);
        ticket.setPromoCodeUsed(promoCode);

        // Update ticket type's sold tickets
        ticketType.setSoldTickets(ticketType.getSoldTickets() + quantity);

        // Save the ticket
        return ticketsRepository.save(ticket);
    }

    // Convert entity to DTO
    public TicketsDto convertToDto(Tickets ticket) {
        return TicketsDto.builder()
                .isTicketActive(ticket.isTicketActive())
                .fees(ticket.getFees())
                .vat(ticket.getVat())
                .totalAmount(ticket.getTotalAmount())
                .quantity(ticket.getQuantity())
                .ticketTypeId(ticket.getId().getTicketTypeId())
                .paymentStatus(ticket.getPaymentStatus())
                .build();
    }

    @Transactional
    public Tickets confirmPayment(TicketId ticketId) {
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
    public void deleteTicket(TicketId ticketId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (PaymentStatus.COMPLETED.equals(ticket.getPaymentStatus())) {
            throw new RuntimeException("Cannot delete a ticket with completed payment");
        }

        EventTicketTypes ticketType = ticket.getTicketType();
        ticketType.setSoldTickets(ticketType.getSoldTickets() - ticket.getQuantity());

        ticketsRepository.delete(ticket);
    }

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
        return event.getTicketTypes().stream()
                .mapToInt(EventTicketTypes::getSoldTickets)
                .sum();
    }

    @Override
    public int countTicketsAvailableForEvent(Events event) {
        return event.getRemainingTickets();
    }

    @Override
    public boolean checkTicketAvailability(Events event, String ticketTypeName, int quantityRequested) {
        return event.getTicketTypes().stream()
                .filter(tt -> tt.getName().equals(ticketTypeName))
                .findFirst()
                .map(tt -> tt.getTotalTickets() - tt.getSoldTickets() >= quantityRequested)
                .orElse(false);
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
