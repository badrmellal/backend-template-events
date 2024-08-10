package backend.event_management_system.exceptions;

public class EmailExistException extends Exception{
    public EmailExistException(String message) {
        super(message);
    }
}
