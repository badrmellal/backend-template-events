package backend.event_management_system.models;


import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
public class Tickets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private  Events event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private LocalDate purchaseDate;
    private String ticketType;
    private float ticketPrice;
    private int quantity;
    private boolean isTicketActive;

    public Tickets(){

    }

    public Tickets(Long id, Events event, Users user, LocalDate purchaseDate, String ticketType, float ticketPrice, int quantity, boolean isTicketActive) {
        this.id = id;
        this.event = event;
        this.user = user;
        this.purchaseDate = purchaseDate;
        this.ticketType = ticketType;
        this.ticketPrice = ticketPrice;
        this.quantity = quantity;
        this.isTicketActive = isTicketActive;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Events getEvent() {
        return event;
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public float getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(float ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isTicketActive() {
        return isTicketActive;
    }

    public void setTicketActive(boolean ticketActive) {
        isTicketActive = ticketActive;
    }
}
