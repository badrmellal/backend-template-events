package backend.event_management_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventTicketTypes {
    private String name;
    private float price;
    private String currency;
    private int totalTickets;
    @Getter
    private int soldTickets;
    private boolean isFree;
    private String ticketTypeId;

    public EventTicketTypes() {}

    public EventTicketTypes(String name, String ticketTypeId, float price, String currency, int totalTickets, boolean isFree) {
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.totalTickets = totalTickets;
        this.soldTickets = 0; // Initialize sold tickets to 0
        this.isFree = isFree;
        this.ticketTypeId = ticketTypeId;
    }


    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean isFree) {
        this.isFree = isFree;
    }

    public int getRemainingTickets() {
        return totalTickets - soldTickets;
    }

}
