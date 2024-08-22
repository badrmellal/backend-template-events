package backend.event_management_system.controller;

import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.jwt.JwtTokenProvider;
import backend.event_management_system.models.Events;
import backend.event_management_system.models.Users;
import backend.event_management_system.service.EventsService;
import backend.event_management_system.service.FilteredEvents;
import backend.event_management_system.service.S3Service;
import backend.event_management_system.service.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = {"/events"})
@CrossOrigin(origins = "http://localhost:3000")
public class EventsController {

    private final EventsService eventsService;
    private final UsersService usersService;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;

    public EventsController(EventsService eventsService, UsersService usersService, JwtTokenProvider jwtTokenProvider, S3Service s3Service) {
        this.eventsService = eventsService;
        this.usersService = usersService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.s3Service = s3Service;
    }


    @GetMapping(path = {"/authenticated-basic-user"})
    public String basicUserLoggedIn() {
        return "This text is from the backend, and later it will be a beautiful list of events. cheers !";
    }

    @GetMapping(path = {"/home"})
    public List<Events> getApprovedEvents(@RequestParam Optional<FilteredEvents> filteredEvents) {
        return eventsService.getApprovedEvents(filteredEvents);
    }

    @GetMapping(path = {"/search"})
    public List<Events> getEventsByKeywordSearch(@RequestParam String keyword){
        return eventsService.getEventBySearch(keyword);
    }

    @GetMapping("/{id}")
    public Events getEventById(@PathVariable Long id){
        return eventsService.getEventById(id);
    }

    @GetMapping("/publisher/{username}")
    public List<Events> getEventsByUsername(@PathVariable String username){
        return eventsService.getEventsByUsername(username);
    }

    @GetMapping("/availability/{id}")
    public boolean checkEventAvailability(@PathVariable Long id){
        return eventsService.checkEventAvailability(id);
    }


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('event:create')")
    public ResponseEntity<Events> createEvent(@RequestHeader("Authorization") String token,
                                              @RequestParam("eventName") String eventName,
                                              @RequestParam("eventCategory") String eventCategory,
                                              @RequestParam("eventDescription") String eventDescription,
                                              @RequestParam("eventPrice") float eventPrice,
                                              @RequestParam("eventDate") String eventDate,
                                              @RequestParam("addressLocation") String addressLocation,
                                              @RequestParam("googleMapsUrl") String googleMapsUrl,
                                              @RequestParam("totalTickets") int totalTickets,
                                              @RequestParam("eventImage") MultipartFile eventImage,
                                              @RequestParam(value = "eventVideo", required = false) MultipartFile eventVideo) throws ParseException {

        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));

        String imageUrl = s3Service.uploadEventFile(eventName, eventImage);
        String videoUrl = eventVideo != null ? s3Service.uploadEventFile(eventName, eventVideo) : "";

        Events event = new Events();

        event.setEventManagerUsername(email);
        event.setEventName(eventName);
        event.setEventCategory(eventCategory);
        event.setEventDescription(eventDescription);
        event.setEventImage(imageUrl);
        event.setEventVideo(videoUrl);
        event.setEventPrice(eventPrice);
        event.setEventDate(new SimpleDateFormat("yyyy-MM-dd").parse(eventDate)); // to format the date
        event.setAddressLocation(addressLocation);
        event.setGoogleMapsUrl(googleMapsUrl);
        event.setTotalTickets(totalTickets);
        event.setRemainingTickets(totalTickets); // Initialize remaining tickets to total tickets

        return ResponseEntity.ok(eventsService.createEvent(event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('event:update')")
    public Events updateEvent(@PathVariable Long id, @RequestBody Events event){
        return eventsService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('event:delete')")
    public void deleteEvent(@PathVariable Long id){
         eventsService.deleteEvent(id);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('event:create')")
    public List<Events> getEventsPendingApproval(){
        return eventsService.getEventsPendingApproval();
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('event:approve')")
    public Events approveEvent(@PathVariable Long id){
        return eventsService.approveEvents(id);
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('event:deny')")
    public Events rejectEvent(@PathVariable Long id){
        return eventsService.rejectEvents(id);
    }

}
