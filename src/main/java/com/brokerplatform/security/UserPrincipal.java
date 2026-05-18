package com.brokerplatform.security;

import com.brokerplatform.entity.Broker;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean locked;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(Long id, String email, String password,
                          boolean locked, boolean active,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.locked = locked;
        this.active = active;
        this.authorities = authorities;
    }

    public static UserPrincipal of(Broker broker) {
        List<SimpleGrantedAuthority> authorities = broker.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        return new UserPrincipal(
                broker.getId(),
                broker.getEmail(),
                broker.getPasswordHash(),
                broker.isLocked(),
                broker.getStatus() == Broker.Status.ACTIVE,
                authorities
        );
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public boolean isAccountNonLocked() { return !locked; }
    @Override public boolean isEnabled() { return active; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
