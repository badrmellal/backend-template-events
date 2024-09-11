package backend.event_management_system.repository;

import backend.event_management_system.models.Events;
import backend.event_management_system.models.PaymentStatus;
import backend.event_management_system.models.Tickets;
import backend.event_management_system.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketsRepository extends JpaRepository<Tickets, Long> {

    List<Tickets> findByUser(Users user);
    List<Tickets> findByEvent(Events event);
    int countByEvent(Events event);
    List<Tickets> findByUserAndEvent(Users user, Events event);
    List<Tickets> findByPaymentStatus(PaymentStatus paymentStatus);

}
