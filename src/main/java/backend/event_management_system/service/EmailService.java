package backend.event_management_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendVerificationEmail(String to, String token){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@myticket.africa");
            message.setTo(to);
            message.setSubject("Email verification");
            message.setText("Please click the link below to verify your email:\n"
                    + "http://localhost:8080/user/verify?token=" + token);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@myticket.africa");
            message.setTo(to);
            message.setSubject("Password Reset Request");
            message.setText("You've requested to reset your password. Click the link below to set a new password:\n"
                    + "http://localhost:8080/reset-password?token=" + token);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean sendOrganizationInvitationEmail(String to, String organizationName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@myticket.africa");
            message.setTo(to);
            message.setSubject("Invitation to Join " + organizationName);
            message.setText("You have been invited to join " + organizationName + " on myticket.africa "
                    + "Click the link below to accept the invitation:\n"
                    + "http://localhost:8080/organization/accept-invitation?token=" + token);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
