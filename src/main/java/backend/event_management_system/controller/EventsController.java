package backend.event_management_system.controller;

import backend.event_management_system.dto.EventsDto;
import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.jwt.JwtTokenProvider;

import backend.event_management_system.models.EventTicketTypes;
import backend.event_management_system.models.Events;
import backend.event_management_system.service.EventsService;
import backend.event_management_system.service.FilteredEvents;
import backend.event_management_system.service.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@RestController
@RequestMapping(path = {"/events"})
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class EventsController {

    private final EventsService eventsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;

    private LocalDateTime parseEventDate(String eventDate) {
        return LocalDateTime.ofInstant(Instant.parse(eventDate), ZoneId.of("UTC"));
    }



    @GetMapping(path = {"/authenticated-basic-user"})
    public String basicUserLoggedIn() {
        return "This text is from the backend, and later it will be a beautiful list of events. cheers !";
    }

    @GetMapping(path = {"/home"})
    public ResponseEntity<List<EventsDto>> getApprovedEvents(
            @RequestParam Optional<FilteredEvents> filteredEvents) {
        return ResponseEntity.ok(eventsService.getApprovedEvents(filteredEvents).stream().map(eventsService::getEventsDto).toList());
    }


    @GetMapping("/{eventId}")
    public EventsDto getEventById(@PathVariable Long eventId){
            return eventsService.getEventsDto(eventsService.getEventById(eventId));
    }

    @GetMapping("/publisher/{tokenEmail}")
    @PreAuthorize("hasAuthority('event:create')")
    public List<EventsDto> getEventsByUsername(@PathVariable String tokenEmail) throws EmailNotFoundException {
        return eventsService.getEventsByUsername(tokenEmail, true);
    }

    @GetMapping("/availability/{id}")
    public boolean checkEventAvailability(@PathVariable Long id){
        return eventsService.checkEventAvailability(id);
    }


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('event:create')")
    public ResponseEntity<EventsDto> createEvent(@RequestHeader("Authorization") String token,
                                              @RequestParam("eventName") String eventName,
                                              @RequestParam("eventCategory") String eventCategory,
                                              @RequestParam("eventDescription") String eventDescription,
                                              @RequestParam("isFreeEvent") boolean isFreeEvent,
                                              @RequestParam("eventCurrency") String eventCurrency,
                                              @RequestParam("eventDate") String eventDate,
                                              @RequestParam("addressLocation") String addressLocation,
                                              @RequestParam("googleMapsUrl") String googleMapsUrl,
                                              @RequestParam("ticketTypes") String ticketTypesJson,
                                              @RequestParam("eventImages") List<MultipartFile> eventImages,
                                              @RequestParam(value = "eventVideo", required = false) MultipartFile eventVideo) throws ParseException, JsonProcessingException {

        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Events event = new Events();

        // Handling multiple image uploads
        for (MultipartFile imageFile : eventImages) {
            String imageUrl = s3Service.uploadEventFile(eventName, imageFile);
            event.addEventImage(imageUrl);
        }

        // Handling video upload
        if (eventVideo != null) {
            String videoUrl = s3Service.uploadEventFile(eventName, eventVideo);
            event.setEventVideo(videoUrl);
        }

        LocalDateTime localDateTime = parseEventDate(eventDate);
        event.setEventDate(localDateTime);

        event.setEventManagerUsername(email);
        event.setEventName(eventName);
        event.setEventCategory(eventCategory);
        event.setEventDescription(eventDescription);
        event.setFreeEvent(isFreeEvent);
        event.setApproved(false);
        event.setEventCurrency(eventCurrency);
        event.setEventCreationDate(LocalDateTime.now());
        event.setEventDate(localDateTime);
        event.setAddressLocation(addressLocation);
        event.setGoogleMapsUrl(googleMapsUrl);

        ObjectMapper objectMapper = new ObjectMapper();
        List<EventTicketTypes> ticketTypes = objectMapper.readValue(ticketTypesJson, new TypeReference<List<EventTicketTypes>>() {});
        // Generating ticketTypeId for each ticket type
                for (EventTicketTypes ticketType : ticketTypes) {
                    ticketType.setTicketTypeId(UUID.randomUUID().toString());
                    ticketType.setSoldTickets(0);
                    ticketType.setFree(isFreeEvent);
                    ticketType.setCurrency(eventCurrency);
                }

                event.setTicketTypes(ticketTypes);
        return ResponseEntity.ok(eventsService.getEventsDto(eventsService.createEvent(event)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('event:update')")
    public ResponseEntity<EventsDto> updateEvent(@PathVariable Long id,
                                              @RequestParam("eventName") String eventName,
                                              @RequestParam("eventCategory") String eventCategory,
                                              @RequestParam("eventDescription") String eventDescription,
                                              @RequestParam("isFreeEvent") boolean isFreeEvent,
                                              @RequestParam("eventCurrency") String eventCurrency,
                                              @RequestParam("eventDate") String eventDate,
                                              @RequestParam("addressLocation") String addressLocation,
                                              @RequestParam("googleMapsUrl") String googleMapsUrl,
                                              @RequestParam("ticketTypes") String ticketTypesJson,
                                              @RequestParam("totalTickets") int totalTickets,
                                              @RequestParam(value = "eventImages", required = false) List<MultipartFile> newEventImages,
                                              @RequestParam(value = "eventVideo", required = false) MultipartFile eventVideo) throws ParseException, JsonProcessingException {

        Events updatedEvent = new Events();
        LocalDateTime localDateTime = parseEventDate(eventDate);

        updatedEvent.setEventDate(localDateTime);

        updatedEvent.setId(id);
        updatedEvent.setEventName(eventName);
        updatedEvent.setEventCategory(eventCategory);
        updatedEvent.setEventDescription(eventDescription);
        updatedEvent.setFreeEvent(isFreeEvent);
        updatedEvent.setEventCurrency(eventCurrency);
        updatedEvent.setAddressLocation(addressLocation);
        updatedEvent.setGoogleMapsUrl(googleMapsUrl);
        updatedEvent.setTotalTickets(totalTickets);

        // new images
        if (newEventImages != null && !newEventImages.isEmpty()) {
            List<String> newImageUrls = new ArrayList<>();
            for (MultipartFile imageFile : newEventImages) {
                String imageUrl = s3Service.uploadEventFile(eventName, imageFile);
                newImageUrls.add(imageUrl);
            }
            updatedEvent.setEventImages(newImageUrls);
        }

        // handle video
        if (eventVideo != null) {
            String videoUrl = s3Service.uploadEventFile(eventName, eventVideo);
            updatedEvent.setEventVideo(videoUrl);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<EventTicketTypes> updatedTicketTypes = objectMapper.readValue(ticketTypesJson, new TypeReference<List<EventTicketTypes>>() {});

        Events updated = eventsService.updateEvent(id, updatedEvent, updatedTicketTypes);
        return ResponseEntity.ok(eventsService.getEventsDto(updated));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAuthority('event:delete')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId){
       try {
           Events event = eventsService.getEventById(eventId);
           for (String imageUrl : event.getEventImages()){
               s3Service.deleteEventFile(imageUrl);
           }
           if (event.getEventVideo() != null){
               s3Service.deleteEventFile(event.getEventVideo());
           }
           eventsService.deleteEvent(eventId);
           return ResponseEntity.ok().body("Event deleted successfully");
       } catch (Exception e){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
       }
    }


    @PutMapping("/approve/{eventId}")
    @PreAuthorize("hasAuthority('event:approve')")
    public ResponseEntity<?> approveEvent(@PathVariable Long eventId) {
        try {
            Events approvedEvent = eventsService.approveEvents(eventId);
            return ResponseEntity.ok(approvedEvent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error approving event: " + e.getMessage());
        }
    }

    @PutMapping("/reject/{eventId}")
    @PreAuthorize("hasAuthority('event:deny')")
    public Events rejectEvent(@PathVariable Long eventId){
        return eventsService.rejectEvents(eventId);
    }

    @GetMapping
    public ResponseEntity<List<EventsDto>> getAllEvents(
            @RequestParam Optional<FilteredEvents> filteredEvents) {
        return ResponseEntity.ok(eventsService.getAllEvents(filteredEvents).stream().map(eventsService::getEventsDto).toList());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('event:create')")
    public ResponseEntity<List<EventsDto>> getEventsPendingApproval() {
        return ResponseEntity.ok(eventsService.getEventsPendingApproval().stream().map(eventsService::getEventsDto).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventsDto>> searchEvents(
            @RequestParam String keyword) {
        return ResponseEntity.ok(eventsService.searchEvents(keyword).stream().map(eventsService::getEventsDto).toList());
    }
}
