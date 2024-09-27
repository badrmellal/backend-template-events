package backend.event_management_system.repository;

import backend.event_management_system.models.Organization;
import backend.event_management_system.models.OrganizationMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<OrganizationMembership> findMembershipByInvitationToken(String token);
    Optional<Organization> findByOrganizationName(String organizationName);

    Organization findByOwnerEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE organization o " +
            "SET onboarding_complete = true " +
            "FROM users u " +
            "WHERE o.owner_id = u.id AND u.email = :email", nativeQuery = true)
    int completeOnboarding(String email);

    @Query(value = "SELECT COALESCE((SELECT onboarding_complete FROM organization o JOIN users u ON o.owner_id = u.id WHERE u.email = :email), false)", nativeQuery = true)
    boolean isOnboardingComplete(String email);
}
