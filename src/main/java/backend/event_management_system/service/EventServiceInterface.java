package backend.event_management_system.service;

import backend.event_management_system.models.Events;

import java.util.List;
import java.util.Optional;

public interface EventServiceInterface {
    List<Events> getAllEvents(Optional<FilteredEvents> filteredEvents);
    List<Events> getApprovedEvents(Optional<FilteredEvents> filteredEvents);
    List<Events> getEventBySearch(String SearchKeyword);
    Events getEventById(Long id);
    List<Events> getEventsByUsername(String PublisherUsername);
    boolean checkEventAvailability(Long eventId);

    //for publisher
    Events createEvent(Events events);
    Events updateEvent(Long id, Events updatedEvent);
    void deleteEvent(Long id);
    List<Events> getAllEventsCreated (Long userId);

    //the last 2 are for admin only
    List<Events> getEventsPendingApproval();
    Events approveEvents(Long id);
    Events rejectEvents(Long id);

}
