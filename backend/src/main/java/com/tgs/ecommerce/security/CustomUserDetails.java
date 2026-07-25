package com.tgs.ecommerce.security;

import com.tgs.ecommerce.user.domain.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adaptador que expone nuestro {@link User} a Spring Security como
 * {@link UserDetails}.
 *
 * <p>Spring Security convierte cada {@link com.tgs.ecommerce.user.domain.Role
 * Role} en un {@link GrantedAuthority} con prefijo {@code ROLE_} — así se
 * pueden usar expresiones como {@code hasRole('ADMIN')} en
 * {@code @PreAuthorize}.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
            .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
            .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
        return Boolean.TRUE.equals(user.getActive());
    }

    /** Atajo para servicios que necesitan los roles como Strings. */
    public List<String> getRoleNames() {
        return user.getRoles().stream()
            .map(r -> r.getName().name())
            .toList();
    }
}
