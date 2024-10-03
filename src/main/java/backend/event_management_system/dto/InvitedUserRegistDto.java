package backend.event_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitedUserRegistDto {
    private String username;
    private String email;
    private String password;
    private String inviteCode;
}
