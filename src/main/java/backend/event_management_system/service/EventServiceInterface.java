package backend.event_management_system.service;

import backend.event_management_system.models.EventTicketType;
import backend.event_management_system.models.Events;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EventServiceInterface {
    List<Events> getAllEvents(Optional<FilteredEvents> filteredEvents);
    List<Events> searchEvents(String keyword);
    List<Events> getApprovedEvents(Optional<FilteredEvents> filteredEvents);
    Events getEventById(Long id);
    List<Events> getEventsByUsername(String PublisherEmail);
    boolean checkEventAvailability(Long eventId);


    //for publisher
    Events createEvent(Events events, List<EventTicketType> ticketTypes);
    Events updateEvent(Long id, Events updatedEvent, List<EventTicketType> updatedTicketTypes);
    void deleteEvent(Long id);
    List<Events> getAllEventsCreated (Long userId);

    //the last are for admin only
    Events approveEvents(Long id);
    Events rejectEvents(Long id);
    List<Events> getEventsPendingApproval();

}
