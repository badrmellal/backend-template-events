package backend.event_management_system.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventCategory;
    private String eventName;
    private String eventDescription;
    private String eventImage;
    private String eventVideo;
    private float eventPrice;
    private String eventManagerUsername;
    private Date eventDate;
    private String addressLocation;
    private String googleMapsUrl;
    private boolean isApproved;
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventCreationDate;
    private int totalTickets;
    private int remainingTickets;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event", orphanRemoval = true)
    private Set<Tickets> tickets = new HashSet<>();

    public Events() {
    }


    public Events(Long id, String eventCategory, String eventName, boolean isApproved, String eventDescription, String eventImage, String eventVideo, float eventPrice, String eventManagerUsername, Date eventDate, String addressLocation, String googleMapsUrl, int totalTickets, int remainingTickets, Set<Tickets> tickets) {
        this.id = id;
        this.eventCategory = eventCategory;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventImage = eventImage;
        this.eventVideo = eventVideo;
        this.eventPrice = eventPrice;
        this.eventManagerUsername = eventManagerUsername;
        this.eventDate = eventDate;
        this.addressLocation = addressLocation;
        this.googleMapsUrl = googleMapsUrl;
        this.isApproved = false;
        this.totalTickets = totalTickets;
        this.remainingTickets = remainingTickets;
        this.tickets = tickets;
    }

    @PrePersist
    protected void onCreate(){
        this.eventCreationDate = new Date();
    }
    public Date getEventCreationDate() {
        return eventCreationDate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public String getEventVideo() {
        return eventVideo;
    }

    public void setEventVideo(String eventVideo) {
        this.eventVideo = eventVideo;
    }

    public float getEventPrice() {
        return eventPrice;
    }

    public void setEventPrice(float eventPrice) {
        this.eventPrice = eventPrice;
    }

    public String getEventManagerUsername() {
        return eventManagerUsername;
    }

    public void setEventManagerUsername(String eventManagerUsername) {
        this.eventManagerUsername = eventManagerUsername;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getAddressLocation() {
        return addressLocation;
    }

    public void setAddressLocation(String addressLocation) {
        this.addressLocation = addressLocation;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public Set<Tickets> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Tickets> tickets) {
        this.tickets = tickets;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
        this.remainingTickets = totalTickets;  // Initialize remaining tickets to total tickets
    }

    public int getRemainingTickets() {
        return remainingTickets;
    }

    public void setRemainingTickets(int remainingTickets) {
        this.remainingTickets = remainingTickets;
    }

    //custom methods
    public void addTicket(Tickets ticket) {
        this.tickets.add(ticket);
        ticket.setEvent(this);
        this.remainingTickets -= ticket.getQuantity();
    }
    public void removeTicket(Tickets ticket) {
        this.tickets.remove(ticket);
        ticket.setEvent(null);
        this.remainingTickets += ticket.getQuantity();
    }
}
