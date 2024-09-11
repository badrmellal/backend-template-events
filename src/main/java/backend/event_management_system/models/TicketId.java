package backend.event_management_system.models;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TicketId implements Serializable {
    private Long eventId;
    private String ticketTypeId;
    private String sequenceNumber;

    public TicketId() {}

    public TicketId(Long eventId, String ticketTypeId, String sequenceNumber) {
        this.eventId = eventId;
        this.ticketTypeId = ticketTypeId;
        this.sequenceNumber = sequenceNumber;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTicketTypeId() {
        return ticketTypeId;
    }

    public void setTicketTypeId(String ticketTypeId) {
        this.ticketTypeId = ticketTypeId;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketId ticketId = (TicketId) o;
        return Objects.equals(eventId, ticketId.eventId) &&
                Objects.equals(ticketTypeId, ticketId.ticketTypeId) &&
                Objects.equals(sequenceNumber, ticketId.sequenceNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, ticketTypeId, sequenceNumber);
    }
}
