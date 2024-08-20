package backend.event_management_system.service;

import backend.event_management_system.constant.EventsSpecialFiltering;
import backend.event_management_system.models.Events;
import backend.event_management_system.repository.EventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventsService implements EventServiceInterface {

    @Autowired
    private final EventsRepository eventsRepository;

    public EventsService(EventsRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    @Override
    public List<Events> getAllEvents(Optional<FilteredEvents> filteredEvents) {
        if (filteredEvents.isEmpty()){
            return eventsRepository.findAll();
        } else {
            Specification<Events> specialFilter = EventsSpecialFiltering.filterBy(filteredEvents.get());
            return eventsRepository.findAll(specialFilter);
        }
    }

    @Override
    public List<Events> getApprovedEvents(Optional<FilteredEvents> filteredEvents) {
        return filteredEvents.map(events -> eventsRepository.findAll(EventsSpecialFiltering.filterBy(events)))
                .orElseGet(() -> eventsRepository.findByIsApproved(true));
    }


    @Override
    public List<Events> getEventBySearch(String searchKeyword) {
        return eventsRepository.findByEventNameContainingIgnoreCaseOrEventDescriptionContainingIgnoreCase(searchKeyword, searchKeyword);
    }

    @Override
    public Optional<Events> getEventById(Long id) {
        return eventsRepository.findById(id);
    }

    @Override
    public List<Events> getEventsByUsername(String publisherUsername) {
        return eventsRepository.findByEventManagerUsername(publisherUsername);
    }

    @Override
    public boolean checkEventAvailability(Long eventId) {
        return eventsRepository.existsById(eventId);
    }

    @Override
    public Events createEvent(Events event) {
        return eventsRepository.save(event);
    }

    @Override
    public Events updateEvent(Long id, Events updatedEvent) {
        return eventsRepository.findById(id)
                .map(event -> {
                    event.setEventName(updatedEvent.getEventName());
                    event.setEventCategory(updatedEvent.getEventCategory());
                    event.setEventDescription(updatedEvent.getEventDescription());
                    event.setEventImage(updatedEvent.getEventImage());
                    event.setEventVideo(updatedEvent.getEventVideo());
                    event.setEventPrice(updatedEvent.getEventPrice());
                    event.setEventDate(updatedEvent.getEventDate());
                    event.setAddressLocation(updatedEvent.getAddressLocation());
                    event.setGoogleMapsUrl(updatedEvent.getGoogleMapsUrl());
                    return eventsRepository.save(event);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Override
    public void deleteEvent(Long id) {
        eventsRepository.deleteById(id);
    }

    @Override
    public List<Events> getAllEventsCreated(Long userId) {
        // This is for the publisher to get all events that he published
        return eventsRepository.findByEventManagerUsername(userId.toString());
    }

    @Override
    public List<Events> getEventsPendingApproval() {
        // This for publisher and purpose is to get all pending approval events
        return eventsRepository.findByApprovedFalse();
    }

    @Override
    public Events approveEvents(Long id) {
        return eventsRepository.findById(id)
                .map(event -> {
                    event.setApproved(true);
                    return eventsRepository.save(event);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Override
    public Events rejectEvents(Long id) {
        return eventsRepository.findById(id)
                .map(event -> {
                    event.setApproved(false);
                    return eventsRepository.save(event);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }
}
