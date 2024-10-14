package backend.event_management_system.service;

import backend.event_management_system.constant.EventsSpecialFiltering;
import backend.event_management_system.dto.EventsDto;
import backend.event_management_system.dto.UpdateEventDto;
import backend.event_management_system.models.EventTicketTypes;
import backend.event_management_system.models.Events;
import backend.event_management_system.repository.EventsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventsService implements EventServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(EventsService.class);


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

    public EventsDto getEventsDto(Events event){
        return  EventsDto.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .eventCategory(event.getEventCategory())
                .eventDescription(event.getEventDescription())
                .isFreeEvent(event.isFreeEvent())
                .eventCurrency(event.getEventCurrency())
                .eventDate(event.getEventDate())
                .addressLocation(event.getAddressLocation())
                .googleMapsUrl(event.getGoogleMapsUrl())
                .eventImages(event.getEventImages())
                .eventVideo(event.getEventVideo())
                .ticketTypes(event.getTicketTypes())
                .totalTickets(event.getTotalTickets())
                .isApproved(event.isApproved())
                .eventCreationDate(event.getEventCreationDate())
                .eventManagerUsername(event.getEventManagerUsername())
                .build();
    }


    public UpdateEventDto getUpdateEventDto(Events event) {
        List<String> presignedImageUrls = event.getEventImages().stream()
                .map(this::generatePresignedUrl)
                .collect(Collectors.toList());

        String presignedVideoUrl = event.getEventVideo() != null ? generatePresignedUrl(event.getEventVideo()) : null;

        return UpdateEventDto.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .eventCategory(event.getEventCategory())
                .eventDescription(event.getEventDescription())
                .isFreeEvent(event.isFreeEvent())
                .eventCurrency(event.getEventCurrency())
                .eventDate(event.getEventDate())
                .addressLocation(event.getAddressLocation())
                .googleMapsUrl(event.getGoogleMapsUrl())
                .eventImages(presignedImageUrls)
                .eventVideo(presignedVideoUrl)
                .ticketTypes(event.getTicketTypes())
                .totalTickets(event.getTotalTickets())
                .isApproved(event.isApproved())
                .eventCreationDate(event.getEventCreationDate())
                .eventManagerUsername(event.getEventManagerUsername())
                .build();
    }

    private String generatePresignedUrl(String s3Url) {
        String objectKey = extractObjectKeyFromUrl(s3Url);
        return s3Service.generatePresignedUrl(objectKey);
    }

    @Override
    public List<EventsDto> getEventsByUsername(String publisherEmail, Boolean toProcessUrls) {
        List<Events> events = eventsRepository.findByEventManagerUsername(publisherEmail);
        if(!toProcessUrls){
            return events.stream().map(this::getEventsDto).toList();
        }
        for (Events event : events) {
            event = processEventUrls(event);
        }
        return events.stream().map(this::getEventsDto).toList();
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
    public Events updateEvent(Long id, Events updatedEvent, List<EventTicketTypes> updatedTicketTypes) {
        return eventsRepository.findById(id)
                .map(event -> {
                    event.setEventName(updatedEvent.getEventName());
                    event.setEventCategory(updatedEvent.getEventCategory());
                    event.setEventDescription(updatedEvent.getEventDescription());
                    event.setFreeEvent(updatedEvent.isFreeEvent());

                    // Handle images
                    List<String> currentImageUrls = event.getEventImages();
                    List<String> updatedImageUrls = updatedEvent.getEventImages();

                    logger.info("Current image URLs: {}", currentImageUrls);
                    logger.info("Updated image URLs: {}", updatedImageUrls);

                    // Find images to delete (in current but not in updated)
                    List<String> imagesToDelete = currentImageUrls.stream()
                            .filter(url -> !updatedImageUrls.contains(url))
                            .collect(Collectors.toList());

                    logger.info("Images to be deleted: {}", imagesToDelete);

                    // Delete images not present in the update
                    for (String url : imagesToDelete) {
                        try {
                            s3Service.deleteEventFile(url);
                            logger.info("Successfully deleted image: {}", url);
                        } catch (Exception e) {
                            logger.error("Failed to delete image: {}", url, e);
                        }
                    }

                    event.setEventImages(updatedImageUrls);

                    // Handle video
                    if (updatedEvent.getEventVideo() != null && !updatedEvent.getEventVideo().equals(event.getEventVideo())) {
                        if (event.getEventVideo() != null) {
                            s3Service.deleteEventFile(event.getEventVideo());
                        }
                        event.setEventVideo(updatedEvent.getEventVideo());
                    } else if (updatedEvent.getEventVideo() == null && event.getEventVideo() != null) {
                        s3Service.deleteEventFile(event.getEventVideo());
                        event.setEventVideo(null);
                    }


                    event.setEventDate(updatedEvent.getEventDate());
                    event.setApproved(false);  // Mark event as not approved for further review
                    event.setAddressLocation(updatedEvent.getAddressLocation());
                    event.setGoogleMapsUrl(updatedEvent.getGoogleMapsUrl());

                    // Handle ticket types
                    event.getTicketTypes().clear();
                    for (EventTicketTypes ticketType : updatedTicketTypes) {
                        event.addTicketType(ticketType);
                    }

                    return eventsRepository.save(event);  // Save the updated event
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

    protected Events processEventUrls(Events event) {
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

    public List<EventsDto> processEventDtoUrls(List<EventsDto> events) {
        return events.stream()
                .map(this::processEventDtoUrls)
                .collect(Collectors.toList());
    }

    protected EventsDto processEventDtoUrls(EventsDto event) {
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
