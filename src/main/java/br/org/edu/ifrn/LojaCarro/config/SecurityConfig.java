package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.services.UsuarioDetailsService;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final Optional<AuditoriaService> auditoriaService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService,
                          Optional<AuditoriaService> auditoriaService) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.auditoriaService = auditoriaService;
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
                        .requestMatchers("/auditorias/**").hasRole("ADMINISTRADOR")
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
                        .authenticationEntryPoint(new AuditoriaAuthenticationEntryPoint(auditoriaService))
                        .accessDeniedHandler((request, response, exception) -> {
                            auditoriaService.ifPresent(service -> service.registrar(
                                    br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria.ACESSO_NEGADO,
                                    "AUTORIZACAO", null,
                                    br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria.NEGADO,
                                    "Tentativa de acesso sem permissão", request,
                                    org.springframework.security.core.context.SecurityContextHolder
                                            .getContext().getAuthentication()));
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"erro\":\"Acesso negado\"}");
                        }))
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler(new AuditoriaAuthenticationSuccessHandler(auditoriaService))
                        .failureHandler(new AuditoriaAuthenticationFailureHandler(auditoriaService))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(new AuditoriaLogoutSuccessHandler(auditoriaService))
                        .permitAll());

        return http.build();
    }
}
