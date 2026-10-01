package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Optional;

public class AuditoriaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final Optional<AuditoriaService> auditoriaService;

    public AuditoriaAuthenticationSuccessHandler(Optional<AuditoriaService> auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        auditoriaService.ifPresent(service -> service.registrar(
                AcaoAuditoria.LOGIN_SUCESSO, "AUTENTICACAO", null, ResultadoAuditoria.SUCESSO,
                "Login realizado com sucesso", request, authentication));
        response.sendRedirect(request.getContextPath() + "/index.html");
    }
}
