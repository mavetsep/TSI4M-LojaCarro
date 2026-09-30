package br.org.edu.ifrn.LojaCarro.dto;

import jakarta.validation.constraints.NotBlank;

public record UsuarioClienteUpdateRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank(message = "Nome de usuário é obrigatório") String usuario,
        String senha) {
}
