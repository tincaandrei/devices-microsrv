package com.energy.authservice.security;

import com.energy.authservice.entity.Credential;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CredentialUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final List<SimpleGrantedAuthority> authorities;

    public CredentialUserDetails(Credential credential) {
        this.id = credential.getId();
        this.username = credential.getUsername();
        this.password = credential.getPassword();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + credential.getRole().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
