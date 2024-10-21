package backend.event_management_system.dto;


import backend.event_management_system.models.SocialLinks;
import backend.event_management_system.models.Users;
import lombok.*;

@Data
@Builder
public class SocialLinksDto {
    private Long id;
    private String platform;
    private String url;

}
