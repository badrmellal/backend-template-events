package backend.event_management_system.service;

import backend.event_management_system.constant.FeesCalculator;
import backend.event_management_system.constant.TicketSequenceGenerator;
import backend.event_management_system.dto.EventsDto;
import backend.event_management_system.dto.TicketsDto;
import backend.event_management_system.dto.UsersDto;
import backend.event_management_system.models.*;
import backend.event_management_system.repository.EventsRepository;
import backend.event_management_system.repository.TicketsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
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

    @Transactional
    public List<TicketsDto> getTicketsByEvents(List<EventsDto> events) {
        List<Tickets> tickets = new ArrayList<>();
        for (EventsDto event : events) {
            tickets.addAll(ticketsRepository.findByEventId(event.getId()));
        }

        return tickets.stream()
                .map(this::convertToDto)
                .toList();
    }


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

        double ticketPrice = ticketType.getPrice();
        boolean isOrganization = user.isOrganization();
        String currencyCode = event.getEventCurrency();

        FeesCalculator.FeeCalculationResult feeResult = FeesCalculator.calculateFeesAndCommission(
                ticketPrice, quantity, isOrganization, currencyCode);

        float totalAmount = (float) feeResult.totalToCharge;

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
        ticket.setPaymentFees((float) feeResult.stripeFee);
        ticket.setCommission((float) feeResult.commission);
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
        UsersDto usersDto = UsersDto.builder()
                .id(ticket.getUser().getId())
                .username(ticket.getUser().getUsername())
                .email(ticket.getUser().getEmail())
                .phoneNumber(ticket.getUser().getPhoneNumber())
                .countryCode(ticket.getUser().getCountryCode())
                .build();
        EventsDto eventsDto = EventsDto.builder()
                .id(ticket.getEvent().getId())
                .eventName(ticket.getEvent().getEventName())
                .eventDescription(ticket.getEvent().getEventDescription())
                .eventDate(ticket.getEvent().getEventDate())
                .eventCurrency(ticket.getEvent().getEventCurrency())
                .build();
        EventTicketTypes eve = ticket.getTicketType();
        return TicketsDto.builder()
                .id(ticket.getId().getSequenceNumber())
                .isTicketActive(ticket.isTicketActive())
                .paymentFees(ticket.getPaymentFees())
                .commission(ticket.getCommission())
                .totalAmount(ticket.getTotalAmount())
                .quantity(ticket.getQuantity())
                .ticketType(eve.getName())
                .paymentStatus(ticket.getPaymentStatus())
                .usersDto(usersDto)
                .purchaseDate(ticket.getPurchaseDate())
                .eventsDto(eventsDto)
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
    public List<Tickets> getTicketsDetails(User user) {

        return List.of();
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
