package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.services.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // CSRF foi desabilitado somente para simplificar os fetches desta atividade acadêmica.
                .userDetailsService(usuarioDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login.html", "/css/**", "/js/**", "/login", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/carro/**")
                        .hasAnyRole("CLIENTE", "VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/carro/getCarro", "/carro/teste")
                        .hasAnyRole("CLIENTE", "VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/carro/salvar", "/carro/updateCarro")
                        .hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/carro/**")
                        .hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/carro/deleteCarro")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/carro/**")
                        .hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/carro/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/usuarios/clientes")
                        .hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/usuarios/clientes")
                        .hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/clientes/**")
                        .hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                        .requestMatchers("/usuarios/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/auth/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, exception) -> response.sendError(403)))
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/index.html", true)
                        .failureUrl("/login.html?erro")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll());

        return http.build();
    }
}
