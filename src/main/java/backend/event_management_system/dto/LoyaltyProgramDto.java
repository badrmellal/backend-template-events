package backend.event_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoyaltyProgramDto {
    private String tier;
    private int currentPoints;
    private int pointsToNextTier;
    private String benefits;
}
