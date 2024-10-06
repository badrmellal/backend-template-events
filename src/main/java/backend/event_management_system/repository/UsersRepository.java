package backend.event_management_system.repository;

import backend.event_management_system.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users>findUserByUsername(String username);
    Optional<Users>findUserByEmail(String email);
    Users findByVerificationToken(String token);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Users> findByInviteCode(String inviteCode);

    @Query("SELECT DISTINCT u FROM Users u LEFT JOIN FETCH u.socialLinks")
    List<Users> findAllWithSocialLinks();

    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.socialLinks WHERE u.email = :email")
    Users findWithSocialLinksByEmail(@Param("email") String email);
}
