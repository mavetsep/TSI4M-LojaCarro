package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.io.IOException;
import java.util.Optional;

public class AuditoriaAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Optional<AuditoriaService> auditoriaService;
    private final LoginUrlAuthenticationEntryPoint delegate = new LoginUrlAuthenticationEntryPoint("/login.html");

    public AuditoriaAuthenticationEntryPoint(Optional<AuditoriaService> auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        auditoriaService.ifPresent(service -> service.registrar(
                AcaoAuditoria.ACESSO_NEGADO, "AUTENTICACAO", null, ResultadoAuditoria.NEGADO,
                "Tentativa de acesso sem autenticação", request, null));
        delegate.commence(request, response, authException);
    }
}
