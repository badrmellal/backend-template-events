package backend.event_management_system.dto;

import backend.event_management_system.models.SocialLinks;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class UsersDto {
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String phoneNumber;
    private  String role;
    private Date joinDate;
    private Date lastLoginDate;
    private Date lastLoginDateDisplay;
    private boolean enabled;
    private String verificationToken;
    private Date verificationTokenExpiryDate;
    private String countryCode;
    private int totalTickets;
    private int loyaltyPoints;
    private String bio;
    private List<SocialLinks> socialLinks;
}
