package backend.event_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationDto {
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String organizationName;
    private String organizationAddress;
    private String organizationCity;
    private String country;
    private String phoneNumber;
    private String businessCategory;
    private String registrationNumber;
}
