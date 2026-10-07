package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.Auditoria;
import br.org.edu.ifrn.LojaCarro.model.Perfil;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.dto.NomeUsuarioApiResponse;
import br.org.edu.ifrn.LojaCarro.exception.RecursoNaoEncontradoException;
import br.org.edu.ifrn.LojaCarro.repository.AuditoriaRepository;
import br.org.edu.ifrn.LojaCarro.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository auditoriaRepository;
    private final TransactionTemplate newTransaction;
    private final UsuarioApiClient usuarioApiClient;

    public AuditoriaService(AuditoriaRepository auditoriaRepository,
                            PlatformTransactionManager transactionManager,
                            UsuarioApiClient usuarioApiClient) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioApiClient = usuarioApiClient;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    public void registrar(AcaoAuditoria acao, String recurso, Long recursoId,
                          ResultadoAuditoria resultado, String detalhes) {
        registrar(acao, recurso, recursoId, resultado, detalhes,
                currentRequest(), currentAuthentication(), null);
    }

    public void registrar(AcaoAuditoria acao, String recurso, Long recursoId,
                          ResultadoAuditoria resultado, String detalhes,
                          HttpServletRequest request, Authentication authentication) {
        registrar(acao, recurso, recursoId, resultado, detalhes, request, authentication, null);
    }

    public void registrarLoginFalha(String usuarioInformado, HttpServletRequest request) {
        registrar(AcaoAuditoria.LOGIN_FALHA, "AUTENTICACAO", null, ResultadoAuditoria.ERRO,
                "Tentativa de login inválida: usuario=" + RequestUtils.safe(usuarioInformado, 80),
                request, null, RequestUtils.safe(usuarioInformado, 80));
    }

    public Page<Auditoria> listar(String usuario, String acao, String recurso,
                                 ResultadoAuditoria resultado, LocalDate dataInicial,
                                 LocalDate dataFinal, Pageable pageable) {
        Specification<Auditoria> specification = (root, query, cb) -> cb.conjunction();
        if (usuario != null && !usuario.isBlank()) {
            String filtro = usuario.trim().toLowerCase();
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("usuario")), "%" + filtro + "%"));
        }
        if (acao != null && !acao.isBlank()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("acao"), acao.trim().toUpperCase()));
        }
        if (recurso != null && !recurso.isBlank()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("recurso"), recurso.trim().toUpperCase()));
        }
        if (resultado != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("resultado"), resultado));
        }
        if (dataInicial != null) {
            LocalDateTime inicio = dataInicial.atStartOfDay();
            specification = specification.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dataHora"), inicio));
        }
        if (dataFinal != null) {
            LocalDateTime fimExclusivo = dataFinal.plusDays(1).atStartOfDay();
            specification = specification.and((root, query, cb) ->
                    cb.lessThan(root.get("dataHora"), fimExclusivo));
        }
        return auditoriaRepository.findAll(specification, pageable);
    }

    public Auditoria buscarPorId(Long id) {
        return auditoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Auditoria não encontrada: " + id));
    }

    private void registrar(AcaoAuditoria acao, String recurso, Long recursoId,
                           ResultadoAuditoria resultado, String detalhes,
                           HttpServletRequest request, Authentication authentication,
                           String usuarioForcado) {
        Auditoria auditoria = new Auditoria();
        Long usuarioId = usuarioId(request);
        auditoria.setUsuarioId(usuarioId);
        auditoria.setUsuario(usuarioForcado != null
                ? usuarioForcado
                : nomeUsuario(usuarioId, authentication));
        auditoria.setPerfil(perfil(authentication));
        auditoria.setAcao(RequestUtils.safe(acao.name(), 30));
        auditoria.setRecurso(RequestUtils.safe(recurso, 60));
        auditoria.setRecursoId(recursoId);
        auditoria.setMetodoHttp(request == null ? "N/A" : RequestUtils.safe(request.getMethod(), 10));
        auditoria.setEndpoint(request == null ? "N/A" : RequestUtils.safe(request.getRequestURI(), 255));
        auditoria.setResultado(resultado);
        auditoria.setDetalhes(RequestUtils.safe(detalhes, 1000));
        auditoria.setEnderecoIp(RequestUtils.clientIp(request));

        try {
            newTransaction.executeWithoutResult(status -> auditoriaRepository.save(auditoria));
            LOGGER.info("Auditoria registrada: acao={}, recurso={}, recursoId={}, resultado={}, usuarioId={}, usuario={}",
                    auditoria.getAcao(), auditoria.getRecurso(), auditoria.getRecursoId(),
                    auditoria.getResultado(), auditoria.getUsuarioId(), auditoria.getUsuario());
        } catch (Exception ex) {
            // Auditoria nunca pode impedir a operação original.
            LOGGER.error("Falha ao persistir auditoria: acao={}, recurso={}, resultado={}",
                    auditoria.getAcao(), auditoria.getRecurso(), auditoria.getResultado(), ex);
        }
    }

    private String usuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "ANONIMO";
        }
        return RequestUtils.safe(authentication.getName(), 80);
    }

    private String nomeUsuario(Long usuarioId, Authentication authentication) {
        if (usuarioId == null) {
            return usuario(authentication);
        }
        try {
            NomeUsuarioApiResponse response = usuarioApiClient.buscarNome(usuarioId);
            if (response != null && response.nome() != null && !response.nome().isBlank()) {
                return RequestUtils.safe(response.nome(), 80);
            }
        } catch (Exception ex) {
            // A indisponibilidade da API de usuários não pode impedir a operação nem o log.
            LOGGER.warn("Não foi possível consultar o nome do usuário {} na API externa: {}",
                    usuarioId, ex.getMessage());
        }
        return usuario(authentication);
    }

    private Long usuarioId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String value = request.getHeader("X-Usuario-Id");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            LOGGER.warn("Cabeçalho X-Usuario-Id inválido recebido: {}", RequestUtils.safe(value, 30));
            return null;
        }
    }

    private String perfil(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "ANONIMO";
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .map(value -> RequestUtils.safe(value, 20))
                .orElse(Perfil.CLIENTE.name());
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private Authentication currentAuthentication() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    }
}
