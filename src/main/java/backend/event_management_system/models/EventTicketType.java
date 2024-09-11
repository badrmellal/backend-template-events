package backend.event_management_system.models;

import jakarta.persistence.Embeddable;

@Embeddable
public class EventTicketType {
    private String name;
    private float price;
    private int totalTickets;
    private int remainingTickets;

    public EventTicketType() {}

    public EventTicketType(String name, float price, int totalTickets) {
        this.name = name;
        this.price = price;
        this.totalTickets = totalTickets;
        this.remainingTickets = totalTickets;
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

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getRemainingTickets() {
        return remainingTickets;
    }

    public void setRemainingTickets(int remainingTickets) {
        this.remainingTickets = remainingTickets;
    }
}
