package backend.event_management_system.constant;

public class Authority {
    public static final String[] BASIC_USER_AUTHORITIES = { "user:read", "user:update", "event:read" };
    public static final String[] PUBLISHER_AUTHORITIES = { "user:read", "user:update", "event:read", "event:create", "event:update", "event:delete" };
    public static final String[] ORGANIZATION_MEMBER_AUTHORITIES = { "user:read", "user:update", "event:read", "event:create", "event:update", "event:delete", "member:read" };
    public static final String[] ORGANIZATION_AUTHORITIES = { "user:read", "user:update", "event:read", "event:create", "event:update", "event:delete", "member:add", "member:read", "member:update", "member:remove" };
    public static final String[] ADMIN_AUTHORITIES = { "user:read", "user:update", "user:create", "user:delete", "event:read", "event:create", "event:update", "event:approve", "event:deny", "event:delete" };
}