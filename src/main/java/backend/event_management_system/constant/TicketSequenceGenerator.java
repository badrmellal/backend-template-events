package backend.event_management_system.constant;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TicketSequenceGenerator {
    public static String generateSequenceNumber() {
        return UUID.randomUUID().toString();
    }
}
