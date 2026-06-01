package it.univaq.mastereat.dto.web.cart;

import java.io.Serializable;
import java.math.BigDecimal;

public class WebCartItemCharacteristic implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long idCaratteristica;
    private final Long idGruppoCaratteristiche;
    private final String nome;
    private final BigDecimal differenzaPrezzo;

    public WebCartItemCharacteristic(long idCaratteristica,
                                     Long idGruppoCaratteristiche,
                                     String nome,
                                     BigDecimal differenzaPrezzo) {
        this.idCaratteristica = idCaratteristica;
        this.idGruppoCaratteristiche = idGruppoCaratteristiche;
        this.nome = nome;
        this.differenzaPrezzo = differenzaPrezzo != null ? differenzaPrezzo : BigDecimal.ZERO;
    }

    public long getIdCaratteristica() {
        return idCaratteristica;
    }

    public Long getIdGruppoCaratteristiche() {
        return idGruppoCaratteristiche;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getDifferenzaPrezzo() {
        return differenzaPrezzo;
    }
}
