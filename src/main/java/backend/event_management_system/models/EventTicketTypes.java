package backend.event_management_system.models;

import jakarta.persistence.Embeddable;

@Embeddable
public class EventTicketTypes {
    private String name;
    private float price;
    private String currency;
    private int totalTickets;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(int soldTickets) {
        this.soldTickets = soldTickets;
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

    public String getTicketTypeId() {
        return ticketTypeId;
    }

    public void setTicketTypeId(String ticketTypeId) {
        this.ticketTypeId = ticketTypeId;
    }
}
