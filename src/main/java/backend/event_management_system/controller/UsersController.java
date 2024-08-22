package backend.event_management_system.controller;

import backend.event_management_system.exceptions.*;
import backend.event_management_system.models.Roles;
import backend.event_management_system.models.Users;
import backend.event_management_system.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = {"/user"})
@CrossOrigin(origins = "http://localhost:3000")
public class UsersController {

    private final UsersService usersService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UsersController(UsersService usersService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.usersService = usersService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @PostMapping(path = {"/register"})
    public ResponseEntity<?> registerUser(@RequestParam("username") String username,
                                          @RequestParam("email") String email,
                                          @RequestParam("password") String password,
                                          @RequestParam("role") Roles role) {
        try {
            Users newUser = usersService.register(username, email, password, role.name());
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (EmailExistException | UsernameExistException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during registration.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(path = {"/login"})
    public ResponseEntity<String> loginUser(@RequestParam("email") String email,
                            @RequestParam("password") String password
                            ) throws UserNotFoundException, EmailNotFoundException {
        try {
            String serverResponseToken = usersService.login(email, password);
            return new ResponseEntity<>(serverResponseToken, HttpStatus.OK);
        } catch (UserNotFoundException | EmailNotFoundException exception){
            return new ResponseEntity<>("Invalid email or password.", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(path = {"/update-profile-image"})
    public ResponseEntity<Users> updateProfileImage(@RequestParam("username") String username,
                                                    @RequestParam("profileImage") MultipartFile profileImage)
        throws UserNotFoundException, EmailNotFoundException, NotValidImageException, IOException{
        Users updatedUserProfileImage = usersService.updateProfileImage(username, profileImage);
        return new ResponseEntity<>(updatedUserProfileImage, HttpStatus.OK);
    }

    @GetMapping(path = {"/all-users"})
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<List<Users>> getAllUsers() {

       List<Users> usersList = usersService.getUsers();
        return new ResponseEntity<>(usersList, HttpStatus.OK);
    }

}
