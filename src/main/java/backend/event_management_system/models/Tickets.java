package backend.event_management_system.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class Tickets {

    @EmbeddedId
    private TicketId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ticket_type_id", nullable = false)
//    private EventTicketType ticketType;

    private LocalDateTime purchaseDate;
    private int quantity;
    private boolean isTicketActive;
    private float fees;
    private float vat;
    private float totalAmount;
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String promoCodeUsed;

    public Tickets() {
    }

    public Tickets(TicketId id, Events event, Users user, LocalDateTime purchaseDate,
                   int quantity, boolean isTicketActive, float fees, float vat, float totalAmount,
                   String paymentMethod, PaymentStatus paymentStatus, String promoCodeUsed) {
        this.id = id;
        this.event = event;
        this.user = user;
        this.purchaseDate = purchaseDate;
        this.quantity = quantity;
        this.isTicketActive = isTicketActive;
        this.fees = fees;
        this.vat = vat;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.promoCodeUsed = promoCodeUsed;
    }

    public EventTicketTypes getTicketType() {
        return event.getTicketTypeById(id.getTicketTypeId());
    }


}
