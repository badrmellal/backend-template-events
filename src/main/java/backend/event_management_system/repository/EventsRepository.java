package backend.event_management_system.repository;

import backend.event_management_system.models.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventsRepository extends JpaRepository<Events, Long>, JpaSpecificationExecutor<Events> {
    List<Events> findByEventNameContainingIgnoreCaseOrEventDescriptionContainingIgnoreCase(String eventName, String eventDescription);
    List<Events> findByEventManagerUsername(String eventManagerUsername);
    List<Events> findByApprovedFalse();
    List<Events> findByIsApproved(boolean isApproved);

}
