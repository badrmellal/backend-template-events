package backend.event_management_system.dto;

import backend.event_management_system.models.EventTicketTypes;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventsDto {
    private Long id;
    private String eventCategory;
    private String eventName;
    private String eventDescription;
    private String eventVideo;
    private String eventCurrency;
    private boolean isFreeEvent;
    private String eventManagerUsername;
    private LocalDateTime eventDate;
    private String addressLocation;
    private String googleMapsUrl;
    private boolean isApproved;
    private LocalDateTime eventCreationDate;
    private int totalTickets;
    private int remainingTickets;
    private List<String> eventImages;
    private List<EventTicketTypes> ticketTypes;
}
