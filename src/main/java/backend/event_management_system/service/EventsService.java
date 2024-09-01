package backend.event_management_system.service;

import backend.event_management_system.constant.EventsSpecialFiltering;
import backend.event_management_system.models.Events;
import backend.event_management_system.repository.EventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class EventsService implements EventServiceInterface {

    private final EventsRepository eventsRepository;
    private final S3Service s3Service;
    private final String bucketName;

    @Autowired
    public EventsService(EventsRepository eventsRepository, S3Service s3Service, @Value("${aws.s3.bucket}") String bucketName) {
        this.eventsRepository = eventsRepository;
        this.s3Service = s3Service;
        this.bucketName = bucketName;
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
    public Events getEventById(Long id) {
        return eventsRepository.findById(id).orElse(null);
    }


    @Override
    public List<Events> getEventsByUsername(String publisherEmail) {
        List<Events> events = eventsRepository.findByEventManagerUsername(publisherEmail);
        for (Events event: events){
            String objectKey = extractObjectKeyFromUrl(event.getEventImage());
            String presignedUrl = s3Service.generatePresignedUrl(objectKey);
            event.setEventImage(presignedUrl);
        }
        return events;
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
        return eventsRepository.findByIsApproved(false);
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




//    private String extractObjectKeyFromUrl(String url) {
//        // This method assumes the URL format is: https://bucket-name.s3.region.amazonaws.com/object-key
//        // Adjust this logic if your S3 URL format is different
//        String[] parts = url.split(bucketName + "\\.");
//        if (parts.length > 1) {
//            return parts[1].substring(parts[1].indexOf("/") + 1);
//        }
//        throw new IllegalArgumentException("Invalid S3 URL format");
//    }
private String extractObjectKeyFromUrl(String url) {
    try {
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String[] parts = decodedUrl.split(bucketName + "\\.");
        if (parts.length > 1) {
            return parts[1].substring(parts[1].indexOf("/") + 1);
        }
        throw new IllegalArgumentException("Invalid S3 URL format");
    } catch (Exception e) {
        throw new RuntimeException("Error decoding URL", e);
    }
}
}
