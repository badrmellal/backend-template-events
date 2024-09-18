package backend.event_management_system.dto;

import lombok.Data;

@Data
public class UsersDto {
    private String username;
    private String email;
    private  String role;
    private String[] authorities;
}
