package backend.event_management_system.service;

import backend.event_management_system.models.Users;
import backend.event_management_system.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class VerificationTokenService {

    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private LoyaltyService loyaltyService;

    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public void createVerificationToken(Users user, String token) {
        user.setVerificationToken(token);
        user.setVerificationTokenExpiryDate(calculateExpiryDate(48 * 60)); // 2 days
        userRepository.save(user);
    }

    public Users validateVerificationToken(String token) {
        Users user = userRepository.findByVerificationToken(token);
        if (user == null || user.getVerificationTokenExpiryDate().before(new Date())) {
            return null;
        }
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiryDate(null);
        // adding loyalty points before saving the user
        Users inviter = user.getInvitedBy();
        if (inviter != null) {
            loyaltyService.addInviterLoyaltyPoints(inviter);
        }

        userRepository.save(user);
        return user;
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
