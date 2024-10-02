package backend.event_management_system.controller;

import backend.event_management_system.dto.OrganizationDto;
import backend.event_management_system.jwt.JwtTokenProvider;
import backend.event_management_system.models.Organization;
import backend.event_management_system.models.Users;
import backend.event_management_system.service.OrganizationService;
import backend.event_management_system.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @PostMapping(path = {"/create"})
    @PreAuthorize("hasAuthority('member:add')")
    public ResponseEntity<?> createOrganization(@RequestBody OrganizationDto organizationDetails, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users owner = usersService.findUserByEmail(email);
            Organization organization = organizationService.createOrganization(
                    owner,
                    organizationDetails.getFirstName(),
                    organizationDetails.getLastName(),
                    organizationDetails.getJobTitle(),
                    organizationDetails.getOrganizationName(),
                    organizationDetails.getOrganizationAddress(),
                    organizationDetails.getOrganizationCity(),
                    organizationDetails.getCountry(),
                    organizationDetails.getPhoneNumber(),
                    organizationDetails.getBusinessCategory(),
                    organizationDetails.getRegistrationNumber()
            );

            return new ResponseEntity<>(organization, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/complete-onboarding")
    public ResponseEntity<?> completeOnboarding(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(jwtToken);
            boolean updated = organizationService.completeOnboarding(email);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Onboarding completed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No organization found for the given email");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/onboarding-status")
    public ResponseEntity<?> getOnboardingStatus(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(jwtToken);
            boolean isComplete = organizationService.isOnboardingComplete(email);
            return ResponseEntity.ok(Map.of("isComplete", isComplete));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{organizationId}/invite")
    @PreAuthorize("hasAuthority('member:add')")
    public ResponseEntity<?> inviteUserToOrganization(
            @PathVariable Long organizationId,
            @RequestBody Map<String, String> inviteDetails,
            HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users currentUser = usersService.findUserByEmail(email);

            if (!organizationService.getOrganizationById(organizationId).getOwner().equals(currentUser)) {
                return new ResponseEntity<>("You are not authorized to invite users to this organization", HttpStatus.FORBIDDEN);
            }

            organizationService.inviteUserToOrganization(organizationId, inviteDetails.get("email"));
            return new ResponseEntity<>("Invitation sent successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/verify-invitation/{token}")
    public ResponseEntity<?> verifyInvitation(@PathVariable String token) {
        try {
            Map<String, String> invitationDetails = organizationService.verifyInvitationToken(token);
            return new ResponseEntity<>(invitationDetails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/accept-invitation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> acceptInvitation(@RequestBody Map<String, String> acceptanceDetails, HttpServletRequest request) {
        try {
            String jwtToken = getTokenFromRequest(request);
            String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);

            String invitationToken = acceptanceDetails.get("invitationToken");

            organizationService.acceptInvitation(invitationToken, userEmail);
            return new ResponseEntity<>("Invitation accepted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{organizationId}/members/{userId}")
    @PreAuthorize("hasAuthority('member:remove')")
    public ResponseEntity<?> removeUserFromOrganization(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users currentUser = usersService.findUserByEmail(email);

            if (!organizationService.getOrganizationById(organizationId).getOwner().equals(currentUser)) {
                return new ResponseEntity<>("You are not authorized to remove users from this organization", HttpStatus.FORBIDDEN);
            }

            organizationService.removeUserFromOrganization(organizationId, userId);
            return new ResponseEntity<>("User removed from organization successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{organizationId}/members")
    @PreAuthorize("hasAuthority('member:read')")
    public ResponseEntity<?> getOrganizationMembers(@PathVariable Long organizationId) {
        try {
            List<Users> members = organizationService.getOrganizationMembers(organizationId);
            return new ResponseEntity<>(members, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{organizationId}/pending-invitations")
    @PreAuthorize("hasAuthority('member:read')")
    public ResponseEntity<?> getPendingInvitations(@PathVariable Long organizationId, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users currentUser = usersService.findUserByEmail(email);

            // Check if the current user is the owner of the organization
            if (!organizationService.getOrganizationById(organizationId).getOwner().equals(currentUser)) {
                return new ResponseEntity<>("You are not authorized to view pending invitations for this organization", HttpStatus.FORBIDDEN);
            }

            List<Users> pendingInvitations = organizationService.getPendingInvitations(organizationId);
            return new ResponseEntity<>(pendingInvitations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<?> getOrganizationById(@PathVariable Long organizationId) {
        try {
            Organization organization = organizationService.getOrganizationById(organizationId);
            return new ResponseEntity<>(organization, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<?> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        return new ResponseEntity<>(organizations, HttpStatus.OK);
    }

    @PutMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('member:update')")
    public ResponseEntity<?> updateOrganization(
            @PathVariable Long organizationId,
            @RequestBody Map<String, String> organizationDetails,
            HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users currentUser = usersService.findUserByEmail(email);

            // to check if the current user is the owner of the organization
            if (!organizationService.getOrganizationById(organizationId).getOwner().equals(currentUser)) {
                return new ResponseEntity<>("You are not authorized to update this organization", HttpStatus.FORBIDDEN);
            }

            Organization updatedOrganization = organizationService.updateOrganization(
                    organizationId,
                    organizationDetails.get("organizationName"),
                    organizationDetails.get("organizationAddress"),
                    organizationDetails.get("organizationCity"),
                    organizationDetails.get("organizationCountry"),
                    organizationDetails.get("phoneNumber"),
                    organizationDetails.get("businessCategory"),
                    organizationDetails.get("registrationNumber")
            );

            return new ResponseEntity<>(updatedOrganization, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('event:delete')")
    public ResponseEntity<?> deleteOrganization(@PathVariable Long organizationId, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users currentUser = usersService.findUserByEmail(email);
            List<GrantedAuthority> authorities = jwtTokenProvider.listOfAuthoritiesGrantedToAuthenticatedUser(token);

            // to check if the current user is the owner of the organization or has admin authority
            if (!organizationService.getOrganizationById(organizationId).getOwner().equals(currentUser)
                    && !authorities.stream().anyMatch(a -> a.getAuthority().equals("user:delete"))) {
                return new ResponseEntity<>("You are not authorized to delete this organization", HttpStatus.FORBIDDEN);
            }

            organizationService.deleteOrganization(organizationId);
            return new ResponseEntity<>("Organization deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
