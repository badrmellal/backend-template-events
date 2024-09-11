package backend.event_management_system.service;

import backend.event_management_system.constant.EventsSpecialFiltering;
import backend.event_management_system.models.EventTicketType;
import backend.event_management_system.models.Events;
import backend.event_management_system.repository.EventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<Events> getApprovedEvents(Optional<FilteredEvents> filteredEvents) {
        if (filteredEvents.isEmpty()) {
            return processEventUrls(eventsRepository.findByIsApproved(true));
        } else {
            Specification<Events> specialFilter = EventsSpecialFiltering.filterBy(filteredEvents.get());
            return processEventUrls(eventsRepository.findAll(specialFilter.and((root, query, cb) -> cb.isTrue(root.get("isApproved")))));
        }
    }


    @Override
    public List<Events> searchEvents(String keyword) {
        return processEventUrls(eventsRepository.findByEventNameContainingIgnoreCaseOrEventDescriptionContainingIgnoreCase(keyword, keyword));
    }

    @Override
    public Events getEventById(Long id) {
        Optional<Events> eventOptional = eventsRepository.findById(id);
        if (eventOptional.isPresent()) {
            Events event = eventOptional.get();
            return processEventUrls(event);
        }
        return null;
    }


    @Override
    public List<Events> getEventsByUsername(String publisherEmail) {
        List<Events> events = eventsRepository.findByEventManagerUsername(publisherEmail);
        for (Events event : events) {
            List<String> presignedUrls = new ArrayList<>();
            for (String imageUrl : event.getEventImages()) {
                String objectKey = extractObjectKeyFromUrl(imageUrl);
                String presignedUrl = s3Service.generatePresignedUrl(objectKey);
                presignedUrls.add(presignedUrl);
            }
            event.setEventImages(presignedUrls);

            if (event.getEventVideo() != null) {
                String videoObjectKey = extractObjectKeyFromUrl(event.getEventVideo());
                String presignedVideoUrl = s3Service.generatePresignedUrl(videoObjectKey);
                event.setEventVideo(presignedVideoUrl);
            }
        }
        return events;
    }

    @Override
    public boolean checkEventAvailability(Long eventId) {
        return eventsRepository.existsById(eventId);
    }

    @Override
    public Events createEvent(Events event, List<EventTicketType> ticketTypes) {
        for (EventTicketType ticketType : ticketTypes) {
            event.addTicketType(ticketType);
        }
        return eventsRepository.save(event);
    }

    @Override
    public Events updateEvent(Long id, Events updatedEvent, List<EventTicketType> updatedTicketTypes) {
        return eventsRepository.findById(id)
                .map(event -> {
                    event.setEventName(updatedEvent.getEventName());
                    event.setEventCategory(updatedEvent.getEventCategory());
                    event.setEventDescription(updatedEvent.getEventDescription());
                    event.setFreeEvent(updatedEvent.isFreeEvent());
                    if (updatedEvent.getEventImages() != null && !updatedEvent.getEventImages().isEmpty()) {
                        event.setEventImages(updatedEvent.getEventImages());
                    }
                    if (updatedEvent.getEventVideo() != null) {
                        event.setEventVideo(updatedEvent.getEventVideo());
                    }
                    event.setEventDate(updatedEvent.getEventDate());
                    event.setApproved(false);
                    event.setAddressLocation(updatedEvent.getAddressLocation());
                    event.setGoogleMapsUrl(updatedEvent.getGoogleMapsUrl());
                    event.getTicketTypes().clear();
                    for (EventTicketType ticketType : updatedTicketTypes) {
                        event.addTicketType(ticketType);
                    }
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

    @Override
    public List<Events> getAllEvents(Optional<FilteredEvents> filteredEvents) {
        if (filteredEvents.isEmpty()) {
            List<Events> events = eventsRepository.findAll();
            return processEventUrls(events);
        } else {
            Specification<Events> specialFilter = EventsSpecialFiltering.filterBy(filteredEvents.get());
            return processEventUrls(eventsRepository.findAll(specialFilter));
        }
    }


    @Override
    public List<Events> getEventsPendingApproval() {
        List<Events> pendingEvents = eventsRepository.findByIsApproved(false);
        return processEventUrls(pendingEvents);
    }


//    private String extractObjectKeyFromUrl(String url) {
//        String[] parts = url.split(bucketName + "\\.");
//        if (parts.length > 1) {
//            return parts[1].substring(parts[1].indexOf("/") + 1);
//        }
//        throw new IllegalArgumentException("Invalid S3 URL format");
//    }

    private List<Events> processEventUrls(List<Events> events) {
        for (Events event : events) {
            event = processEventUrls(event);
        }
        return events;
    }

    private Events processEventUrls(Events event) {
        List<String> presignedUrls = new ArrayList<>();
        for (String imageUrl : event.getEventImages()) {
            String objectKey = extractObjectKeyFromUrl(imageUrl);
            String presignedUrl = s3Service.generatePresignedUrl(objectKey);
            presignedUrls.add(presignedUrl);
        }
        event.setEventImages(presignedUrls);

        if (event.getEventVideo() != null) {
            String videoObjectKey = extractObjectKeyFromUrl(event.getEventVideo());
            String presignedVideoUrl = s3Service.generatePresignedUrl(videoObjectKey);
            event.setEventVideo(presignedVideoUrl);
        }
        return event;
    }



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
