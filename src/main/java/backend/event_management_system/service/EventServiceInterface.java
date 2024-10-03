package backend.event_management_system.service;

import backend.event_management_system.dto.EventsDto;
import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.models.EventTicketTypes;
import backend.event_management_system.models.Events;

import java.util.List;
import java.util.Optional;

public interface EventServiceInterface {
    List<Events> getAllEvents(Optional<FilteredEvents> filteredEvents);
    List<Events> searchEvents(String keyword);
    List<Events> getApprovedEvents(Optional<FilteredEvents> filteredEvents);
    Events getEventById(Long id);
    List<EventsDto> getEventsByUsername(String PublisherEmail,Boolean toProcessUrls) throws EmailNotFoundException;
    boolean checkEventAvailability(Long eventId);


    //for publisher
    Events createEvent(Events events);
    Events updateEvent(Long id, Events updatedEvent, List<EventTicketTypes> updatedTicketTypes);
    void deleteEvent(Long id);
    List<Events> getAllEventsCreated (Long userId);

    //the last are for admin only
    Events approveEvents(Long id);
    Events rejectEvents(Long id);
    List<Events> getEventsPendingApproval();

}
