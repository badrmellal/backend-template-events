package backend.event_management_system.controller;

import backend.event_management_system.dto.InvitedUserRegistDto;
import backend.event_management_system.dto.LoyaltyProgramDto;
import backend.event_management_system.dto.UsersDto;
import backend.event_management_system.exceptions.*;
import backend.event_management_system.jwt.JwtTokenProvider;
import backend.event_management_system.models.Roles;
import backend.event_management_system.models.Users;
import backend.event_management_system.service.LoyaltyService;
import backend.event_management_system.service.UsersService;
import backend.event_management_system.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = {"/user"})
@CrossOrigin(origins = "http://localhost:3000")

public class UsersController {

    private final UsersService usersService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoyaltyService loyaltyService;

    @Autowired
    public UsersController(UsersService usersService, JwtTokenProvider jwtTokenProvider, BCryptPasswordEncoder bCryptPasswordEncoder, VerificationTokenService verificationTokenService, LoyaltyService loyaltyService) {
        this.usersService = usersService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.verificationTokenService = verificationTokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loyaltyService = loyaltyService;
    }

    @GetMapping(path = {"/user-info"})
    public UsersDto getUser(@RequestHeader("Authorization") String token) throws EmailNotFoundException {
              String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
              Users user = usersService.findUserByEmail(email);
              return UsersDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .profileImageUrl(user.getProfileImageUrl())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole())
                        .joinDate(user.getJoinDate())
                        .lastLoginDate(user.getLastLoginDate())
                        .lastLoginDateDisplay(user.getLastLoginDateDisplay())
                        .enabled(user.isEnabled())
                        .verificationToken(user.getVerificationToken())
                        .verificationTokenExpiryDate(user.getVerificationTokenExpiryDate())
                        .totalTickets(user.getTotalTickets())
                        .loyaltyPoints(user.getLoyaltyPoints())
                          .build();
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
    public ResponseEntity<List<UsersDto>> getAllUsers() {

       List<Users> usersList = usersService.getUsers();
       List<UsersDto> usersDtos = usersList.stream()
               .map(this::convertToDto)
               .toList();
        return ResponseEntity.ok(usersDtos);
    }
    private UsersDto convertToDto(Users user){
        return UsersDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .joinDate(user.getJoinDate())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginDateDisplay(user.getLastLoginDateDisplay())
                .enabled(user.isEnabled())
                .countryCode(user.getCountryCode())
                .build();
    }

    @GetMapping("/loyalty-points")
    public ResponseEntity<Integer> getLoyaltyPoints(@RequestHeader("Authorization") String token) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        int loyaltyPoints = loyaltyService.getLoyaltyPoints(user);
        return ResponseEntity.ok(loyaltyPoints);
    }

    @GetMapping("/generate-invite-code")
    public ResponseEntity<String> generateInviteCode(@RequestHeader("Authorization") String token) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        String inviteCode = loyaltyService.generateInviteCode(user);
        return ResponseEntity.ok(inviteCode);
    }

    @GetMapping("/validate-invite/{code}")
    public ResponseEntity<Map<String, Boolean>> validateInviteCode(@PathVariable String code) {
        boolean isValid = usersService.validateInviteCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-invited")
    public ResponseEntity<?> registerInvitedUser(@RequestBody InvitedUserRegistDto registrationDto) {
        try {
            Users newUser = usersService.registerInvitedUser(registrationDto);
            return new ResponseEntity<>("Registration successful. Please check your email to verify your account.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/loyalty-program")
    public ResponseEntity<LoyaltyProgramDto> getLoyaltyProgram(@RequestHeader("Authorization") String token) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        LoyaltyProgramDto loyaltyProgram = loyaltyService.getLoyaltyProgram(user);
        return ResponseEntity.ok(loyaltyProgram);
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
    public ResponseEntity<Users> updateUser(@PathVariable Long id,
                                            @RequestParam String assignedRole,
                                            @RequestParam(required = false) String phoneNumber,
                                            @RequestParam(required = false) String countryCode,
                                            @RequestParam boolean enabled) {
        try {
            Users user = usersService.updateUser(id, assignedRole, phoneNumber, countryCode, enabled);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-user-info")
    public ResponseEntity<UsersDto> updateUserInfo(@RequestBody UsersDto usersDto) {
        try {
            System.out.println(usersDto);
            Users updatedUser = usersService.updateUserInfo(usersDto);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user-total-tickets")
    @PreAuthorize("hasAuthority('event:read')")
    public ResponseEntity<Integer> getUserTotalTickets(@RequestHeader("Authorization") String token) throws EmailNotFoundException {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
        Users user = usersService.findUserByEmail(email);
        return ResponseEntity.ok(user.getTotalTickets());
    }

    @DeleteMapping(path = {"/delete/{id}"})
    @PreAuthorize("hasAuthority('user:delete')")
    public void deleteUser(@PathVariable Long id) throws UserNotFoundException {
        usersService.deleteUser(id);
    }

}
