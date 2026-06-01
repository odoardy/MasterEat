package it.univaq.mastereat.dto.web.owner;

import java.math.BigDecimal;

public class OwnerIngredienteResponse {

    private long id;
    private long idProdotto;
    private String nome;
    private String unitaMisura;
    private BigDecimal quantita;
    private boolean allergene;
    private boolean attivo;

    public OwnerIngredienteResponse() {
    }

    public OwnerIngredienteResponse(long id,
                                    long idProdotto,
                                    String nome,
                                    String unitaMisura,
                                    BigDecimal quantita,
                                    boolean allergene,
                                    boolean attivo) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.nome = nome;
        this.unitaMisura = unitaMisura;
        this.quantita = quantita;
        this.allergene = allergene;
        this.attivo = attivo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    public BigDecimal getQuantita() {
        return quantita;
    }

    public void setQuantita(BigDecimal quantita) {
        this.quantita = quantita;
    }

    public boolean isAllergene() {
        return allergene;
    }

    public void setAllergene(boolean allergene) {
        this.allergene = allergene;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }
}
