package backend.event_management_system.dto;

import backend.event_management_system.models.PaymentStatus;
import backend.event_management_system.models.TicketId;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketsDto {
    private float quantity;
    private String id;
    private float fees;
    private float vat;
    private float totalAmount;
    private boolean isTicketActive;
    private String ticketType;
    private PaymentStatus paymentStatus;
    private LocalDateTime purchaseDate;
    private UsersDto usersDto;
    private EventsDto eventsDto;
}
