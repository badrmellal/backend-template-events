package backend.event_management_system.models;

import static backend.event_management_system.constant.Authority.ADMIN_AUTHORITIES;
import static backend.event_management_system.constant.Authority.BASIC_USER_AUTHORITIES;
import static backend.event_management_system.constant.Authority.ORGANIZATION_AUTHORITIES;
import static backend.event_management_system.constant.Authority.ORGANIZATION_MEMBER_AUTHORITIES;
import static backend.event_management_system.constant.Authority.PUBLISHER_AUTHORITIES;

public enum Roles { 
   ROLE_BASIC_USER(BASIC_USER_AUTHORITIES),
   ROLE_PUBLISHER(PUBLISHER_AUTHORITIES),
   ROLE_ORGANIZATION_MEMBER(ORGANIZATION_MEMBER_AUTHORITIES),
   ROLE_ORGANIZATION_OWNER(ORGANIZATION_AUTHORITIES),
   ROLE_ADMIN(ADMIN_AUTHORITIES);

   private String[] authorities;

   Roles(String... authorities) {
    this.authorities = authorities;
   }
   public String[] getAuthorities() {
    return authorities;
   }
}
