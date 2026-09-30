package br.org.edu.ifrn.LojaCarro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioClienteCreateRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank(message = "Nome de usuário é obrigatório") String usuario,
        @NotBlank(message = "Senha é obrigatória no cadastro")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres") String senha) {
}
