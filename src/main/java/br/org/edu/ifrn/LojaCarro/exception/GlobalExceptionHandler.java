package br.org.edu.ifrn.LojaCarro.exception;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Optional<AuditoriaService> auditoriaService;

    public GlobalExceptionHandler(Optional<AuditoriaService> auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @ExceptionHandler(CarroException.class)
    public ResponseEntity<Map<String, String>> handleCarroException(CarroException ex,
                                                                      HttpServletRequest request) {
        registrarErro(request, "Dados inválidos para carro", ResultadoAuditoria.ERRO);
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RecursoNaoEncontradoException ex,
                                                               HttpServletRequest request) {
        registrarErro(request, "Recurso não encontrado", ResultadoAuditoria.ERRO);
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UsuarioDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(UsuarioDuplicadoException ex,
                                                                HttpServletRequest request) {
        registrarErro(request, "Usuário duplicado", ResultadoAuditoria.ERRO);
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<Map<String, String>> handleInvalidData(DadosInvalidosException ex,
                                                                  HttpServletRequest request) {
        registrarErro(request, "Dados inválidos", ResultadoAuditoria.ERRO);
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AcessoNegadoException ex,
                                                                   HttpServletRequest request) {
        registrarErro(request, "Acesso negado", ResultadoAuditoria.NEGADO);
        return response(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        registrarErro(request, "Dados inválidos", ResultadoAuditoria.ERRO);
        Map<String, Object> body = new HashMap<>();
        body.put("erro", "Dados inválidos");
        body.put("campos", ex.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage(),
                        (first, second) -> first)));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableMessage(HttpMessageNotReadableException ex,
                                                                        HttpServletRequest request) {
        registrarErro(request, "Dados inválidos", ResultadoAuditoria.ERRO);
        return response(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                    HttpServletRequest request) {
        registrarErro(request, "Violação de integridade dos dados", ResultadoAuditoria.ERRO);
        return response(HttpStatus.CONFLICT, "Já existe um registro com os mesmos dados únicos");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                   HttpServletRequest request) {
        registrarErro(request, "Dados inválidos", ResultadoAuditoria.ERRO);
        return response(HttpStatus.BAD_REQUEST, "Parâmetro da requisição inválido");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        registrarErro(request, "Método HTTP não suportado", ResultadoAuditoria.ERRO);
        return response(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(NoResourceFoundException ex,
                                                                       HttpServletRequest request) {
        registrarErro(request, "Recurso não encontrado", ResultadoAuditoria.ERRO);
        return response(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOGGER.error("Erro inesperado na requisição: tipo={}, endpoint={}",
                ex.getClass().getSimpleName(), request.getRequestURI());
        registrarErro(request, "Erro inesperado", ResultadoAuditoria.ERRO);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno");
    }

    private void registrarErro(HttpServletRequest request, String detalhes, ResultadoAuditoria resultado) {
        auditoriaService.ifPresent(service -> service.registrar(
                AcaoAuditoria.ERRO, recurso(request), recursoId(request), resultado, detalhes));
    }

    private String recurso(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/carro")) {
            return "CARRO";
        }
        if (path.startsWith("/usuarios")) {
            return "USUARIO";
        }
        if (path.startsWith("/auditorias")) {
            return "AUDITORIA";
        }
        if (path.startsWith("/api/auth")) {
            return "AUTENTICACAO";
        }
        return "REQUISICAO";
    }

    private Long recursoId(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String[] partes = path.split("/");
        for (int i = partes.length - 1; i >= 0; i--) {
            try {
                if (!partes[i].isBlank()) {
                    return Long.valueOf(partes[i]);
                }
            } catch (NumberFormatException ignored) {
                // O endpoint pode não possuir um identificador numérico.
            }
        }
        return null;
    }

    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("erro", message);
        return ResponseEntity.status(status).body(body);
    }
}
