package backend.event_management_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Entity
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Setter
@Getter
public class Users implements Serializable, UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String countryCode;
    private String phoneNumber;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userPassword;
    private  String role;
    private String[] authorities;
    private Date joinDate;
    private Date lastLoginDate;
    private Date lastLoginDateDisplay;
    private boolean enabled;
    private String verificationToken;
    private Date verificationTokenExpiryDate;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tickets> tickets = new HashSet<>();

    @OneToOne(mappedBy = "owner")
    private Organization ownedOrganization;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrganizationMembership> memberships = new HashSet<>();

    public void setRole(String role) {
        this.role = role;
        // automatically update the authorities based on the role
        this.authorities = getAuthoritiesForRole(role);
    }


    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return stream(this.authorities).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }


    private String[] getAuthoritiesForRole(String role) {
        if (role.equals(Roles.ROLE_ADMIN.name())) {
            return Roles.ROLE_ADMIN.getAuthorities();
        } else if (role.equals(Roles.ROLE_PUBLISHER.name())) {
            return Roles.ROLE_PUBLISHER.getAuthorities();
        } else if (role.equals(Roles.ROLE_BASIC_USER.name())) {
            return Roles.ROLE_BASIC_USER.getAuthorities();
        } else if (role.equals(Roles.ROLE_ORGANIZATION_MEMBER.name())) {
            return Roles.ROLE_ORGANIZATION_MEMBER.getAuthorities();
        } else if (role.equals(Roles.ROLE_ORGANIZATION_OWNER.name())) {
            return Roles.ROLE_ORGANIZATION_OWNER.getAuthorities();

        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

}
