package it.univaq.mastereat.dto.common;

import java.math.BigDecimal;

public class IngredienteProdottoResponse {

    private long id;
    private String nome;
    private BigDecimal quantita;
    private String unitaMisura;

    public IngredienteProdottoResponse() {
    }

    public IngredienteProdottoResponse(long id,
                                       String nome,
                                       BigDecimal quantita,
                                       String unitaMisura) {
        this.id = id;
        this.nome = nome;
        this.quantita = quantita;
        this.unitaMisura = unitaMisura;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getQuantita() {
        return quantita;
    }

    public void setQuantita(BigDecimal quantita) {
        this.quantita = quantita;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }
}
