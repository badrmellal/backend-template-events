package backend.event_management_system.constant;

public class Authority {
    public static final String[] BASIC_USER_AUTHORITIES = { "user:read", "user:update" };
    public static final String[] PUBLISHER_AUTHORITIES = { "user:read", "user:update", "event:create", "event:update", "event:delete" };
    public static final String[] ADMIN_AUTHORITIES = { "user:read", "user:update", "user:create", "user:delete", "event:create", "event:update", "event:delete" };

}
