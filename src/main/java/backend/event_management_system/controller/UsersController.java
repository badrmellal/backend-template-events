package backend.event_management_system.controller;

import backend.event_management_system.exceptions.*;
import backend.event_management_system.models.Roles;
import backend.event_management_system.models.Users;
import backend.event_management_system.service.UsersService;
import backend.event_management_system.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = {"/user"})
@CrossOrigin(origins = "http://localhost:3000")
public class UsersController {

    private final UsersService usersService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VerificationTokenService verificationTokenService;

    @Autowired
    public UsersController(UsersService usersService, BCryptPasswordEncoder bCryptPasswordEncoder, VerificationTokenService verificationTokenService) {
        this.usersService = usersService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.verificationTokenService = verificationTokenService;
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

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            Users user = verificationTokenService.validateVerificationToken(token);
            if (user != null) {
                // Redirect to frontend success page
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/verification-success"))
                        .build();
            } else {
                // Redirect to frontend error page
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/verification-failed"))
                        .build();
            }
        } catch (Exception e) {
            // Redirect to frontend error page
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:3000/verification-error"))
                    .build();
        }
    }

    @PostMapping(path = {"/login"})
    public ResponseEntity<String> loginUser(@RequestParam("email") String email,
                            @RequestParam("password") String password
                            ) throws UserNotFoundException, EmailNotFoundException, EmailNotVerifiedException {
        try {
            String serverResponseToken = usersService.login(email, password);
            return new ResponseEntity<>(serverResponseToken, HttpStatus.OK);
        } catch (UserNotFoundException | EmailNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        } catch (EmailNotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login.");
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

    @GetMapping(path = {"/admin-username/{adminEmail}"})
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<String> getAdminUsername(@PathVariable String adminEmail){
        String username = usersService.getUsernameFromEmail(adminEmail);
        return ResponseEntity.ok(username);
    }

    @GetMapping(path = {"/{publisherEmail}"})
    public ResponseEntity<Users> getPublisherInfo(@PathVariable String publisherEmail){
        Optional<Users> user = usersService.getPublisherInfoFromEmail(publisherEmail);
        return  ResponseEntity.of(user);
    }

    @PutMapping(path = {"/update-role/{id}"})
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Users> updateRole(@PathVariable Long id, @RequestParam String assignedRole) throws EmailNotFoundException {
        try {
            Users user = usersService.updateUserRole(id, assignedRole);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);  // If the role is invalid
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // For any other exceptions
        }
    }

    @DeleteMapping(path = {"/delete/{id}"})
    @PreAuthorize("hasAuthority('user:delete')")
    public void deleteUser(@PathVariable Long id) throws UserNotFoundException {
        usersService.deleteUser(id);
    }

}
