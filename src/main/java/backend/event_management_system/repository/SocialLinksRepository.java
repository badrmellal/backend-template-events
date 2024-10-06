package backend.event_management_system.repository;

import backend.event_management_system.models.SocialLinks;
import backend.event_management_system.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialLinksRepository extends JpaRepository<SocialLinks, Long> {
    List<SocialLinks> findByUser(Users user);
}
