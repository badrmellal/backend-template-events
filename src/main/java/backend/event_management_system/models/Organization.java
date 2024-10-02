package backend.event_management_system.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Getter
@Setter
@RequiredArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String organizationName;
    private String organizationAddress;
    private String organizationCity;
    private String country;
    private String phoneNumber;
    private String businessCategory;
    private String registrationNumber;
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
