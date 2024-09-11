package backend.event_management_system.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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

    public EventTicketType getTicketType() {
        return event.getTicketTypeById(id.getTicketTypeId());
    }

    public TicketId getId(){
        return id;
    }
    public void setId(TicketId id) {
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


    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
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

    public float getFees() {
        return fees;
    }

    public void setFees(float fees) {
        this.fees = fees;
    }

    public float getVat() {
        return vat;
    }

    public void setVat(float vat) {
        this.vat = vat;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPromoCodeUsed() {
        return promoCodeUsed;
    }

    public void setPromoCodeUsed(String promoCodeUsed) {
        this.promoCodeUsed = promoCodeUsed;
    }
}
