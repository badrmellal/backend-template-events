package backend.event_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketsDto {
    private float quantity;
    private float fees;
    private float vat;
    private float totalAmount;
    private boolean isTicketActive;
    private String ticketTypeId;

}