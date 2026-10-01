package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.Auditoria;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
@RestController
@RequestMapping("/auditorias")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public Page<Auditoria> listar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) ResultadoAuditoria resultado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Auditoria> page = auditoriaService.listar(
                usuario, acao, recurso, resultado, dataInicial, dataFinal,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "dataHora")));
        auditoriaService.registrar(AcaoAuditoria.LISTAR, "AUDITORIA", null,
                ResultadoAuditoria.SUCESSO, "Consulta de logs de auditoria");
        return page;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Auditoria> buscar(@PathVariable Long id) {
        Auditoria auditoria = auditoriaService.buscarPorId(id);
        auditoriaService.registrar(AcaoAuditoria.CONSULTAR, "AUDITORIA", id,
                ResultadoAuditoria.SUCESSO, "Consulta de log de auditoria");
        return ResponseEntity.ok(auditoria);
    }
}
