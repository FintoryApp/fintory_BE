package com.fintory.auth.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fintory.domain.child.model.LoginType;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private final String username;
    @JsonIgnore
    private String password;
    private final String nickname;
    private final String role;
    private final LoginType loginType;

    public CustomUserDetails(String username, String password, String nickname, String role, LoginType loginType) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.loginType = loginType;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getPassword() {
        return password;
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override public boolean isEnabled() {
        return true;
    }
}
