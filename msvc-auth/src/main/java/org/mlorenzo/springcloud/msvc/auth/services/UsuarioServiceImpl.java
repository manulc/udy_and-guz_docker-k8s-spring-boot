package org.mlorenzo.springcloud.msvc.auth.services;

import org.mlorenzo.springcloud.msvc.auth.models.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
public class UsuarioServiceImpl implements UserDetailsService {
    private final WebClient webClient;
    private final UserDetailsService inMemoryUserDetailsService;

    public UsuarioServiceImpl(WebClient.Builder webClientBuilder, PasswordEncoder passwordEncoder) {
        this.webClient = webClientBuilder.build();
        this.inMemoryUserDetailsService = initInMemoryUserDetailsService(passwordEncoder);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return inMemoryUserDetailsService.loadUserByUsername(username);
        }
        catch(UsernameNotFoundException exception) {
            // En este caso, el username es el email del usuario
            Usuario usuario = webClient.get()
                    .uri("http://msvc-usuarios/email/" + username)
                    .retrieve()
                    .bodyToMono(Usuario.class)
                    .onErrorResume(WebClientResponseException.class,
                            ex -> ex.getRawStatusCode() == 404
                                    ? Mono.error(new UsernameNotFoundException(ex.getMessage()))
                                    : Mono.error(ex))
                    .block();
            return new User(usuario.getEmail(), usuario.getPassword(), true, true,
                    true, true, Collections.emptyList());
        }
    }

    private UserDetailsService initInMemoryUserDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails userDetails = User.withUsername("admin")
                .password(passwordEncoder.encode("12345"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(userDetails);
    }
}
