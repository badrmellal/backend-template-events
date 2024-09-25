package backend.event_management_system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

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
}
