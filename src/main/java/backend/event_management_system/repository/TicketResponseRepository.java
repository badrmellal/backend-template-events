package backend.event_management_system.repository;

import backend.event_management_system.models.TicketResponse;
import backend.event_management_system.models.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketResponseRepository extends JpaRepository<TicketResponse, Long> {

}
