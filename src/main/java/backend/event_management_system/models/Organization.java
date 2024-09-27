package backend.event_management_system.models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String organizationName;
    private String organizationAddress;
    private String organizationCity;
    private String organizationCountry;
    private String phoneNumber;
    private String businessCategory;
    private String companyRegistrationNumber;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerJobTitle;

    @Column(nullable = false)
    private boolean onboardingComplete = false;

    @Column(unique = true)
    private String invitationToken;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private Users owner;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrganizationMembership> memberships = new HashSet<>();

    public Organization(){
    }

    public Organization(Long id, boolean onboardingComplete, String organizationName, String ownerFirstName, String ownerLastName, String ownerJobTitle, String organizationAddress, String organizationCity, String organizationCountry, String phoneNumber, String businessCategory, String companyRegistrationNumber, String invitationToken, Users owner, Set<OrganizationMembership> memberships) {
        this.id = id;
        this.onboardingComplete = onboardingComplete;
        this.organizationName = organizationName;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.ownerJobTitle = ownerJobTitle;
        this.organizationAddress = organizationAddress;
        this.organizationCity = organizationCity;
        this.organizationCountry = organizationCountry;
        this.phoneNumber = phoneNumber;
        this.businessCategory = businessCategory;
        this.companyRegistrationNumber = companyRegistrationNumber;
        this.invitationToken = invitationToken;
        this.owner = owner;
        this.memberships = memberships;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isOnboardingComplete() {
        return onboardingComplete;
    }

    public void setOnboardingComplete(boolean onboardingComplete) {
        this.onboardingComplete = onboardingComplete;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public void setOwnerLastName(String ownerLastName) {
        this.ownerLastName = ownerLastName;
    }

    public String getOwnerJobTitle() {
        return ownerJobTitle;
    }

    public void setOwnerJobTitle(String ownerJobTitle) {
        this.ownerJobTitle = ownerJobTitle;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationAddress() {
        return organizationAddress;
    }

    public void setOrganizationAddress(String organizationAddress) {
        this.organizationAddress = organizationAddress;
    }

    public String getOrganizationCity() {
        return organizationCity;
    }

    public void setOrganizationCity(String organizationCity) {
        this.organizationCity = organizationCity;
    }

    public String getOrganizationCountry() {
        return organizationCountry;
    }

    public void setOrganizationCountry(String organizationCountry) {
        this.organizationCountry = organizationCountry;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getCompanyRegistrationNumber() {
        return companyRegistrationNumber;
    }

    public void setCompanyRegistrationNumber(String companyRegistrationNumber) {
        this.companyRegistrationNumber = companyRegistrationNumber;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }

    public Users getOwner() {
        return owner;
    }

    public void setOwner(Users owner) {
        this.owner = owner;
    }

    public Set<OrganizationMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(Set<OrganizationMembership> memberships) {
        this.memberships = memberships;
    }


    public void addMember(Users user, MembershipStatus status) {
        String invitationToken = UUID.randomUUID().toString();
        OrganizationMembership membership = new OrganizationMembership(this, user, invitationToken, status);
        memberships.add(membership);
        user.getMemberships().add(membership);
    }

    public void removeMember(Users user) {
        OrganizationMembership membership = memberships.stream()
                .filter(m -> m.getUser().equals(user))
                .findFirst()
                .orElse(null);
        if (membership != null) {
            memberships.remove(membership);
            user.getMemberships().remove(membership);
            membership.setOrganization(null);
            membership.setUser(null);
        }
    }

    public boolean hasMember(Users user) {
        return memberships.stream()
                .anyMatch(m -> m.getUser().equals(user) && m.getStatus() == MembershipStatus.JOINED);
    }

    public boolean hasPendingInvitation(Users user) {
        return memberships.stream()
                .anyMatch(m -> m.getUser().equals(user) && m.getStatus() == MembershipStatus.PENDING);
    }

}
