package backend.event_management_system.models;
import static backend.event_management_system.constant.Authority.*;

public enum Roles {

    ROLE_BASIC_USER(BASIC_USER_AUTHORITIES),
    ROLE_PUBLISHER(PUBLISHER_AUTHORITIES),
    ROLE_ADMIN(ADMIN_AUTHORITIES);

        private String[] authorities;

    Roles(String... authorities){
        this.authorities = authorities;
    }

    public String[] getAuthorities(){
        return authorities;
    }
}
