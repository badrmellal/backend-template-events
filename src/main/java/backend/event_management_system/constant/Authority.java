package backend.event_management_system.constant;

public class Authority {
    public static final String[] BASIC_USER_AUTHORITIES = { "user:read" };
    public static final String[] PUBLISHER_AUTHORITIES = { "user:read", "user:update", "user:create" };
    public static final String[] ADMIN_AUTHORITIES = { "user:read", "user:update", "user:create", "user:delete" };

}
