package backend.event_management_system.repository;

import backend.event_management_system.models.Events;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventsRepository extends JpaRepository<Events, Long>, JpaSpecificationExecutor<Events> {
    List<Events> findByEventManagerUsername(String eventManagerEmail);
    List<Events> findByIsApproved(boolean isApproved);
    List<Events> findByEventNameContainingIgnoreCaseOrEventDescriptionContainingIgnoreCase(String name, String description);

}
