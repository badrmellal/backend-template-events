package backend.event_management_system.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = {"/events"})
@CrossOrigin(origins = "http://localhost:3000")
public class EventsController {

    @GetMapping(path = {"/home"})
    public String homePageDemo(){
        return "This is a demo page used for testing, later we'll build it as soon as tickets are available for purchase.";
    }
}
