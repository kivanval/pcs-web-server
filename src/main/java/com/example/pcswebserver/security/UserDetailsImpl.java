package com.example.pcswebserver.security;

import com.example.pcswebserver.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


public class UserDetailsImpl implements UserDetails {
    private final String username;
    private final String password;

    private final Collection<GrantedAuthority> authorities = new HashSet<>();

    public UserDetailsImpl(User user, Collection<? extends GrantedAuthority> externalAuthorities) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities.addAll(externalAuthorities);
        this.authorities.add(new SimpleGrantedAuthority(username));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
