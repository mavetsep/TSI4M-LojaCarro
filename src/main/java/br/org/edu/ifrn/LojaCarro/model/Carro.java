package br.org.edu.ifrn.LojaCarro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Marca e obrigatoria")
    @Size(max = 255, message = "Marca nao pode ter mais de 255 caracteres")
    private String marca;

    @NotBlank(message = "Modelo e obrigatorio")
    @Size(max = 30, message = "Modelo nao pode ter mais de 30 caracteres")
    private String modelo;

    @NotNull(message = "Ano e obrigatorio")
    @Min(value = 1886, message = "Ano invalido")
    @Max(value = 2026, message = "Ano nao pode ser futuro")
    private Integer ano;

    @NotNull(message = "Preco e obrigatorio")
    @Positive(message = "Preco deve ser positivo")
    private Double preco;

    public Carro() {
    }

    public Carro(String marca, String modelo, Integer ano, Double preco) {
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }
}