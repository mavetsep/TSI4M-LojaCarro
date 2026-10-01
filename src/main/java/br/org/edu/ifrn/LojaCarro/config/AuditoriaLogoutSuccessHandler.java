package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.Optional;

public class AuditoriaLogoutSuccessHandler implements LogoutSuccessHandler {

    private final Optional<AuditoriaService> auditoriaService;

    public AuditoriaLogoutSuccessHandler(Optional<AuditoriaService> auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        auditoriaService.ifPresent(service -> service.registrar(
                AcaoAuditoria.LOGOUT, "AUTENTICACAO", null, ResultadoAuditoria.SUCESSO,
                "Logout realizado", request, authentication));
        response.sendRedirect(request.getContextPath() + "/login.html?logout");
    }
}
