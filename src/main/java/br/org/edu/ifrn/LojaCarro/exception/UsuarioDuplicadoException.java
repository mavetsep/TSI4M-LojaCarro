package br.org.edu.ifrn.LojaCarro.exception;

public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String usuario) {
        super("O nome de usuário já está sendo utilizado: " + usuario);
    }
}
