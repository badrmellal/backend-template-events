package backend.event_management_system.service;

import backend.event_management_system.dto.LoyaltyProgramDto;
import backend.event_management_system.models.Users;
import backend.event_management_system.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LoyaltyService {

    private final UsersRepository usersRepository;

    @Autowired
    public LoyaltyService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Transactional(readOnly = true)
    public int getLoyaltyPoints(Users user) {
        return user.getLoyaltyPoints();
    }

    @Transactional
    public String generateInviteCode(Users user) {
        if (user.getInviteCode() == null || user.getInviteCode().isEmpty()) {
            String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            user.setInviteCode(inviteCode);
            usersRepository.save(user);
            return inviteCode;
        }
        return user.getInviteCode();
    }

    @Transactional(readOnly = true)
    public LoyaltyProgramDto getLoyaltyProgram(Users user) {
        String tier;
        int currentPoints = user.getLoyaltyPoints();
        int pointsToNextTier;
        String benefits;
        if (currentPoints < 100) {
            tier = "Bronze";
            pointsToNextTier = 100 - currentPoints;
            benefits = "0% discount";
        } else if (currentPoints < 500) {
            tier = "Silver";
            pointsToNextTier = 500 - currentPoints;
            benefits = "5% discount on all events";
        } else if (currentPoints < 1000) {
            tier = "Gold";
            pointsToNextTier = 1000 - currentPoints;
            benefits = "10% discount on all events, priority booking";
        } else {
            tier = "Platinum";
            pointsToNextTier = 0;
            benefits = "15% discount on all events, priority booking, exclusive events access";
        }

        return new LoyaltyProgramDto(tier, currentPoints, pointsToNextTier, benefits);
    }

    @Transactional
    public void addLoyaltyPoints(Users user, double purchaseAmount) {
        int pointsToAdd = (int) (purchaseAmount / 10); // 1 point for every $10 spent
        user.setLoyaltyPoints(user.getLoyaltyPoints() + pointsToAdd);
        user.setTotalSpend(user.getTotalSpend() + purchaseAmount);
        usersRepository.save(user);
    }

    @Transactional
    public void addInviterLoyaltyPoints(Users inviter) {
        final int POINTS_FOR_INVITE = 5; // points for every new user invited
        inviter.setLoyaltyPoints(inviter.getLoyaltyPoints() + POINTS_FOR_INVITE);
        usersRepository.save(inviter);
    }

    @Transactional(readOnly = true)
    public float getDiscountPercentage(Users user) {
        int loyaltyPoints = user.getLoyaltyPoints();
        if (loyaltyPoints >= 1000) {
            return 0.15f; // 15% discount for Platinum
        } else if (loyaltyPoints >= 500) {
            return 0.10f; // 10% discount for Gold
        } else if (loyaltyPoints >= 100) {
            return 0.05f; // 5% discount for Silver
        } else {
            return 0; // 0% discount for Bronze
        }
    }
}
