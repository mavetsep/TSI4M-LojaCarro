package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.entity.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/carro")
public class CarroController {

    private final CarroService carroService;

    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @PostMapping("/salvar")
    public ResponseEntity<Carro> salvarCarro(@RequestBody @Valid Carro c) {
        Carro savedCarro = carroService.save(c);
        return ResponseEntity.ok(savedCarro);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(@PathVariable Long id, @RequestBody @Valid Carro c) {
        if (carroService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        c.setId(id);
        Carro updatedCarro = carroService.update(c);
        return ResponseEntity.ok(updatedCarro);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id) {
        if (carroService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        carroService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        Optional<Carro> carro = carroService.findById(id);
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Carro>> pesquisarTodosCarros() {
        List<Carro> carros = carroService.findAll();
        return ResponseEntity.ok(carros);
    }
}