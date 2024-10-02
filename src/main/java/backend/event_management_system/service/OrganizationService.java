package backend.event_management_system.service;

import backend.event_management_system.models.*;
import backend.event_management_system.repository.OrganizationRepository;
import backend.event_management_system.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private EmailService emailService;



    @Transactional
    public boolean completeOnboarding(String email) {
        int updatedRows = organizationRepository.completeOnboarding(email);
        return updatedRows > 0;
    }

    public boolean isOnboardingComplete(String email) {
        return organizationRepository.isOnboardingComplete(email);
    }

    public Organization findByOwnerEmail(String email) {
        return organizationRepository.findByOwnerEmail(email);
    }

    public Organization createOrganization(Users owner, String firstName, String lastName, String jobTitle, String organizationName, String organizationAddress,
                                           String organizationCity, String country, String phoneNumber,
                                           String businessCategory, String registrationNumber) throws Exception {
        if (organizationRepository.findByOrganizationName(organizationName).isPresent()) {
            throw new Exception("An organization with this name already exists");
        }

        Organization organization = new Organization();
        organization.setOwner(owner);
        organization.setOwnerFirstName(firstName);
        organization.setOwnerLastName(lastName);
        organization.setOwnerJobTitle(jobTitle);
        organization.setOrganizationName(organizationName);
        organization.setOrganizationAddress(organizationAddress);
        organization.setOrganizationCity(organizationCity);
        organization.setCountry(country);
        organization.setPhoneNumber(phoneNumber);
        organization.setBusinessCategory(businessCategory);
        organization.setRegistrationNumber(registrationNumber);

        owner.setRole(Roles.ROLE_ORGANIZATION_OWNER.name());
        owner.setAuthorities(Roles.ROLE_ORGANIZATION_OWNER.getAuthorities());
        owner.setOwnedOrganization(organization);

        usersRepository.save(owner);
        return organizationRepository.save(organization);
    }

    public void inviteUserToOrganization(Long organizationId, String userEmail) throws Exception {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exception("Organization not found"));

        Users user = usersRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new Exception("User not found"));

        if (organization.hasMember(user) || organization.hasPendingInvitation(user)) {
            throw new Exception("User is already a member or has a pending invitation");
        }

        organization.addMember(user, MembershipStatus.PENDING);
        Organization savedOrganization = organizationRepository.save(organization);

        OrganizationMembership membership = savedOrganization.getMemberships().stream()
                .filter(m -> m.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new Exception("Failed to create membership"));

        boolean emailSent = emailService.sendOrganizationInvitationEmail(user.getEmail(), organization.getOrganizationName(), membership.getInvitationToken());
        if (!emailSent) {
            throw new Exception("Failed to send invitation email to " + user.getEmail());
        }
    }

    public Map<String, String> verifyInvitationToken(String token) throws Exception {
        OrganizationMembership membership = organizationRepository.findMembershipByInvitationToken(token)
                .orElseThrow(() -> new Exception("Invalid invitation token"));

        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new Exception("Invitation has already been processed");
        }

        Map<String, String> invitationDetails = new HashMap<>();
        invitationDetails.put("organizationName", membership.getOrganization().getOrganizationName());
        invitationDetails.put("userEmail", membership.getUser().getEmail());

        return invitationDetails;
    }

    public void acceptInvitation(String invitationToken, String userEmail) throws Exception {
        OrganizationMembership membership = organizationRepository.findMembershipByInvitationToken(invitationToken)
                .orElseThrow(() -> new Exception("Invalid invitation token"));

        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new Exception("Invitation has already been processed");
        }

        if (!membership.getUser().getEmail().equals(userEmail)) {
            throw new Exception("This invitation is not for the logged-in user");
        }

        membership.setStatus(MembershipStatus.JOINED);
        membership.setInvitationToken(null);
        organizationRepository.save(membership.getOrganization());
    }

    public void removeUserFromOrganization(Long organizationId, Long userId) throws Exception {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exception("Organization not found"));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        organization.removeMember(user);
        organizationRepository.save(organization);
    }

    public List<Users> getOrganizationMembers(Long organizationId) throws Exception {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exception("Organization not found"));

        return organization.getMemberships().stream()
                .filter(m -> m.getStatus() == MembershipStatus.JOINED)
                .map(OrganizationMembership::getUser)
                .collect(Collectors.toList());
    }

    public List<Users> getPendingInvitations(Long organizationId) throws Exception {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exception("Organization not found"));

        return organization.getMemberships().stream()
                .filter(m -> m.getStatus() == MembershipStatus.PENDING)
                .map(OrganizationMembership::getUser)
                .collect(Collectors.toList());
    }

    public Organization getOrganizationById(Long organizationId) throws Exception {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exception("Organization not found"));
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public Organization updateOrganization(Long organizationId, String organizationName, String organizationAddress,
                                           String organizationCity, String country, String phoneNumber,
                                           String businessCategory, String companyRegistrationNumber) throws Exception {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exception("Organization not found"));

        organization.setOrganizationName(organizationName);
        organization.setOrganizationAddress(organizationAddress);
        organization.setOrganizationCity(organizationCity);
        organization.setCountry(country);
        organization.setPhoneNumber(phoneNumber);
        organization.setBusinessCategory(businessCategory);
        organization.setRegistrationNumber(companyRegistrationNumber);

        return organizationRepository.save(organization);
    }

    public void deleteOrganization(Long organizationId) throws Exception {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exception("Organization not found"));

        Users owner = organization.getOwner();
        owner.setRole(Roles.ROLE_PUBLISHER.name());
        owner.setAuthorities(Roles.ROLE_PUBLISHER.getAuthorities());
        owner.setOwnedOrganization(null);
        usersRepository.save(owner);

        organizationRepository.delete(organization);
    }
}
