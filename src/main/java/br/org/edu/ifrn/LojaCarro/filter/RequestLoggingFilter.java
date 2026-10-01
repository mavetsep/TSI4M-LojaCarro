package br.org.edu.ifrn.LojaCarro.filter;

import br.org.edu.ifrn.LojaCarro.util.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/css/") || path.startsWith("/js/")
                || path.equals("/favicon.ico") || path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usuario = "ANONIMO";
            String perfil = "ANONIMO";
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                usuario = RequestUtils.safe(authentication.getName(), 80);
                perfil = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(authority -> authority.startsWith("ROLE_"))
                        .map(authority -> authority.substring("ROLE_".length()))
                        .findFirst().orElse("N/A");
            }
            String message = "Requisição HTTP: metodo=" + request.getMethod()
                    + ", endpoint=" + request.getRequestURI()
                    + ", status=" + response.getStatus()
                    + ", usuario=" + usuario
                    + ", perfil=" + perfil
                    + ", ip=" + RequestUtils.clientIp(request)
                    + ", duracaoMs=" + elapsedMillis;
            if (response.getStatus() >= 500) {
                LOGGER.error(message);
            } else if (response.getStatus() >= 400) {
                LOGGER.warn(message);
            } else {
                LOGGER.info(message);
            }
        }
    }
}
