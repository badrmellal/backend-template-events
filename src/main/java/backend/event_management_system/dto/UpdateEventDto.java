package backend.event_management_system.dto;

import backend.event_management_system.models.EventTicketTypes;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UpdateEventDto {
    private Long id;
    private String eventName;
    private String eventCategory;
    private String eventDescription;
    private List<String> eventImages;
    private String eventVideo;
    private boolean isFreeEvent;
    private String eventCurrency;
    private LocalDateTime eventDate;
    private String addressLocation;
    private String googleMapsUrl;
    private List<EventTicketTypes> ticketTypes;
    private int totalTickets;
    private boolean isApproved;
    private LocalDateTime eventCreationDate;
    private String eventManagerUsername;
}