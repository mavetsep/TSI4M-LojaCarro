
package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import br.org.edu.ifrn.LojaCarro.util.RequestUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/carro")
public class CarroController {

    private final CarroService carroService;
    private final Optional<AuditoriaService> auditoriaService;

    public CarroController(CarroService carroService, Optional<AuditoriaService> auditoriaService) {
        this.carroService = carroService;
        this.auditoriaService = auditoriaService;
    }

    // Salvar carro (corrigido para POST)
    @PostMapping("salvar")
    public ResponseEntity<Carro> salvarCarro(@RequestBody Carro c) {
        Carro savedCarro = carroService.save(c);
        auditar(AcaoAuditoria.CRIAR, savedCarro.getId(), ResultadoAuditoria.SUCESSO,
                "Carro criado: id=" + savedCarro.getId() + ", modelo=" + modeloSeguro(savedCarro));
        return ResponseEntity.ok(savedCarro);
    }

    // Atualizar carro (por ID)
    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(@PathVariable Long id, @RequestBody Carro c) {
        c.setId(id);  // Define o ID no objeto
        Carro updatedCarro = carroService.update(c);
        auditar(AcaoAuditoria.EDITAR, updatedCarro.getId(), ResultadoAuditoria.SUCESSO,
                "Carro alterado: id=" + updatedCarro.getId());
        return ResponseEntity.ok(updatedCarro);
    }

    // Deletar carro (por ID)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id) {
        String modelo = carroService.findById(id).map(Carro::getModelo).orElse(null);
        carroService.deleteById(id);
        auditar(AcaoAuditoria.EXCLUIR, id, ResultadoAuditoria.SUCESSO,
                "Carro excluído: id=" + id + (modelo == null ? "" : ", modelo=" + RequestUtils.safe(modelo, 100)));
        return ResponseEntity.noContent().build();
    }

    // Pesquisar carro por ID
    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        Optional<Carro> carro = carroService.findById(id);
        auditar(AcaoAuditoria.CONSULTAR, id,
                carro.isPresent() ? ResultadoAuditoria.SUCESSO : ResultadoAuditoria.ERRO,
                carro.isPresent() ? "Carro consultado: id=" + id : "Carro não encontrado: id=" + id);
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Pesquisar todos os carros
    @GetMapping("/listarCarros")
    public ResponseEntity<List<Carro>> pesquisarTodosCarros() {
        List<Carro> carros = carroService.findAll();
        auditar(AcaoAuditoria.LISTAR, null, ResultadoAuditoria.SUCESSO, "Lista de carros consultada");
        return ResponseEntity.ok(carros);
    }

    @PostMapping(value = "/getCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> pesquisarCarroPorModelo(@RequestBody String modelo) {
        Optional<Carro> carro = carroService.findByModelo(modelo.trim());
        auditar(AcaoAuditoria.CONSULTAR, carro.map(Carro::getId).orElse(null),
                carro.isPresent() ? ResultadoAuditoria.SUCESSO : ResultadoAuditoria.ERRO,
                carro.isPresent() ? "Carro consultado por modelo" : "Carro não encontrado por modelo");
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/deleteCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deletarCarroPorModelo(@RequestBody String modelo) {
        Carro carro = carroService.deleteByModelo(modelo.trim());
        auditar(AcaoAuditoria.EXCLUIR, carro.getId(), ResultadoAuditoria.SUCESSO,
                "Carro excluído: id=" + carro.getId() + ", modelo=" + modeloSeguro(carro));
        return ResponseEntity.ok("Carro deletado: " + carro.getModelo());
    }

    @PostMapping(value = "/updateCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> atualizarCarroPorModelo(@RequestBody String payload) {
        String[] partes = payload.split(",", 2);
        if (partes.length < 2) {
            throw new CarroException("Payload inválido. Use modelo,preco.");
        }
        String modelo = partes[0].trim();
        double preco = parsePreco(partes[1].trim());
        Carro updatedCarro = carroService.updateByModelo(modelo, preco);
        auditar(AcaoAuditoria.EDITAR, updatedCarro.getId(), ResultadoAuditoria.SUCESSO,
                "Carro alterado: id=" + updatedCarro.getId());
        return ResponseEntity.ok(updatedCarro);
    }

    @PostMapping(value = "/teste", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> bomDia(@RequestBody String nome) {
        auditar(AcaoAuditoria.CONSULTAR, null, ResultadoAuditoria.SUCESSO, "Endpoint de teste consultado");
        return ResponseEntity.ok("Bom dia, " + nome.trim());
    }

    private void auditar(AcaoAuditoria acao, Long recursoId, ResultadoAuditoria resultado, String detalhes) {
        auditoriaService.ifPresent(service -> service.registrar(
                acao, "CARRO", recursoId, resultado, detalhes));
    }

    private String modeloSeguro(Carro carro) {
        return RequestUtils.safe(carro.getModelo(), 100);
    }

    private double parsePreco(String preco) {
        try {
            return Double.parseDouble(preco);
        } catch (NumberFormatException ex) {
            throw new CarroException("Preço inválido: " + preco);
        }
    }
}
