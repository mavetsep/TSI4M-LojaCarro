package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.Optional;

public class AuditoriaAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final Optional<AuditoriaService> auditoriaService;

    public AuditoriaAuthenticationFailureHandler(Optional<AuditoriaService> auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String usuario = request.getParameter("username");
        auditoriaService.ifPresent(service -> service.registrarLoginFalha(usuario, request));
        response.sendRedirect(request.getContextPath() + "/login.html?erro");
    }
}
