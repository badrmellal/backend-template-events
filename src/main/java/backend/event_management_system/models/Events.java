package backend.event_management_system.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Entity
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventCategory;
    private String eventName;
    @Column(length = 1000)
    private String eventDescription;
    @ElementCollection
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url", columnDefinition = "varchar(255)")
    private List<String> eventImages = new ArrayList<>();
    private String eventVideo;
    private String eventCurrency;
    private boolean isFreeEvent;
    private String eventManagerUsername;
    private LocalDateTime eventDate;
    private String addressLocation;
    @Column(length = 1000)
    private String googleMapsUrl;
    private boolean isApproved;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime eventCreationDate;
    @ElementCollection
    @CollectionTable(name = "event_ticket_types", joinColumns = @JoinColumn(name = "event_id"))
    private List<EventTicketTypes> ticketTypes = new ArrayList<>();
    private int totalTickets;
    private int remainingTickets;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event", orphanRemoval = true)
    private Set<Tickets> tickets = new HashSet<>();


    public Events() {
    }


    public Events(Long id, String eventCategory, String eventName, String eventCurrency, boolean isApproved, String eventDescription, boolean isFreeEvent, List<String> eventImages, String eventVideo, String eventManagerUsername , LocalDateTime eventDate, String addressLocation, String googleMapsUrl, int totalTickets, int remainingTickets, Set<Tickets> tickets) {
        this.id = id;
        this.eventCategory = eventCategory;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventImages = eventImages;
        this.eventVideo = eventVideo;
        this.isFreeEvent = isFreeEvent;
        this.eventCurrency = eventCurrency;
        this.eventManagerUsername = eventManagerUsername;
        this.eventDate = eventDate;
        this.addressLocation = addressLocation;
        this.googleMapsUrl = googleMapsUrl;
        this.isApproved = false;
        this.totalTickets = totalTickets;
        this.remainingTickets = remainingTickets;
        this.tickets = tickets;
    }

    public EventTicketTypes getTicketTypeById(String ticketTypeId) {
        return ticketTypes.stream()
                .filter(tt -> tt.getTicketTypeId().equals(ticketTypeId))
                .findFirst()
                .orElse(null);
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

    public boolean isFreeEvent() {
        return isFreeEvent;
    }

    public String getEventCurrency() {
        return eventCurrency;
    }

    public void setEventCurrency(String eventCurrency) {
        this.eventCurrency = eventCurrency;
    }

    public void setFreeEvent(boolean freeEvent) {
        isFreeEvent = freeEvent;
    }


    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }


    public List<String> getEventImages() {
        return eventImages;
    }

    public void setEventImages(List<String> eventImages) {
        this.eventImages = eventImages;
    }

    public void addEventImage(String imageUrl) {
        if (this.eventImages == null) {
            this.eventImages = new ArrayList<>();
        }
        this.eventImages.add(imageUrl);
    }

    public String getEventVideo() {
        return eventVideo;
    }

    public void setEventVideo(String eventVideo) {
        this.eventVideo = eventVideo;
    }


    public String getEventManagerUsername() {
        return eventManagerUsername;
    }

    public void setEventManagerUsername(String eventManagerUsername) {
        this.eventManagerUsername = eventManagerUsername;
    }


    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public LocalDateTime getEventCreationDate() {
        return eventCreationDate;
    }

    public void setEventCreationDate(LocalDateTime eventCreationDate) {
        this.eventCreationDate = eventCreationDate;
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
        this.isApproved = approved;
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


    public List<EventTicketTypes> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(List<EventTicketTypes> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }

    public void removeTicketType(EventTicketTypes ticketType) {
        this.ticketTypes.remove(ticketType);
    }

    public void addTicketType(EventTicketTypes ticketType) {
        if (this.ticketTypes == null) {
            this.ticketTypes = new ArrayList<>();
        }
        this.ticketTypes.add(ticketType);
    }

    public int getRemainingTickets() {
        return this.ticketTypes.stream()
                .mapToInt(EventTicketTypes::getRemainingTickets)
                .sum();
    }

}

