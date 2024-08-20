package backend.event_management_system.controller;

import backend.event_management_system.exceptions.EmailNotFoundException;
import backend.event_management_system.models.Events;
import backend.event_management_system.models.Users;
import backend.event_management_system.service.EventsService;
import backend.event_management_system.service.FilteredEvents;
import backend.event_management_system.service.UsersService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = {"/events"})
@CrossOrigin(origins = "http://localhost:3000")
public class EventsController {

    private final EventsService eventsService;
    private final UsersService usersService;

    public EventsController(EventsService eventsService, UsersService usersService) {
        this.eventsService = eventsService;
        this.usersService = usersService;
    }

    @GetMapping(path = {"/home"})
    public String homePageDemo() {
        return "This is a demo page used for testing, later we'll build it as soon as tickets are available for purchase.";
    }

    @GetMapping(path = {"/authenticated-basic-user"})
    public String basicUserLoggedIn() {
        return "This text is from the backend, and later it will be a beautiful list of events. cheers !";
    }

    @GetMapping(path = {"/home"})
    public List<Events> getApprovedEvents(@RequestParam Optional< FilteredEvents > filteredEvents) {
        return eventsService.getApprovedEvents(filteredEvents);
    }

    @GetMapping(path = {"/search"})
    public List<Events> getEventsByKeywordSearch(@RequestParam String keyword){
        return eventsService.getEventBySearch(keyword);
    }

    @GetMapping("/{id}")
    public Optional<Events> getEventById(@PathVariable Long id){
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
    @PreAuthorize("hasRole('ROLE_PUBLISHER') or hasRole('ROLE_ADMIN')")
    public  Events createEvent(@RequestBody Events event){
        return eventsService.createEvent(event);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PUBLISHER') or hasRole('ROLE_ADMIN')")
    public Events updateEvent(@PathVariable Long id, @RequestBody Events event){
        return eventsService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PUBLISHER') or hasRole('ROLE_ADMIN')")
    public void deleteEvent(@PathVariable Long id){
         eventsService.deleteEvent(id);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Events> getEventsPendingApproval(){
        return eventsService.getEventsPendingApproval();
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Events approveEvent(@PathVariable Long id){
        return eventsService.approveEvents(id);
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Events rejectEvent(@PathVariable Long id){
        return eventsService.rejectEvents(id);
    }

}