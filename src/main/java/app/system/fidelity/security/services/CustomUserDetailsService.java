package app.system.fidelity.security.services;

import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final var user = ofNullable(userRepositoryPort.findByEmail(username))
                .get()
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado!"));

        return CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .authorities(Collections.emptyList())
                .build();
    }

}