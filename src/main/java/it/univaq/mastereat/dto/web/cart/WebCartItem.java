package it.univaq.mastereat.dto.web.cart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WebCartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String itemKey;
    private final long idProdotto;
    private final String nomeProdotto;
    private final BigDecimal prezzoBase;
    private int quantita;
    private final int minutiPreparazione;
    private final List<WebCartItemCharacteristic> caratteristiche;

    public WebCartItem(long idProdotto,
                       String nomeProdotto,
                       BigDecimal prezzoBase,
                       int quantita,
                       int minutiPreparazione,
                       List<WebCartItemCharacteristic> caratteristiche) {
        if (quantita <= 0) {
            throw new IllegalArgumentException("La quantita deve essere maggiore di zero");
        }

        this.idProdotto = idProdotto;
        this.nomeProdotto = nomeProdotto;
        this.prezzoBase = prezzoBase != null ? prezzoBase : BigDecimal.ZERO;
        this.quantita = quantita;
        this.minutiPreparazione = minutiPreparazione;
        this.caratteristiche = caratteristiche != null ? new ArrayList<>(caratteristiche) : new ArrayList<>();
        this.itemKey = buildItemKey(idProdotto, this.caratteristiche);
    }

    public static String buildItemKey(long idProdotto, List<WebCartItemCharacteristic> caratteristiche) {
        String ids = caratteristiche == null || caratteristiche.isEmpty()
                ? ""
                : caratteristiche.stream()
                        .map(WebCartItemCharacteristic::getIdCaratteristica)
                        .sorted(Comparator.naturalOrder())
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

        return idProdotto + ":" + ids;
    }

    public String getItemKey() {
        return itemKey;
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public BigDecimal getPrezzoBase() {
        return prezzoBase;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        if (quantita <= 0) {
            throw new IllegalArgumentException("La quantita deve essere maggiore di zero");
        }
        this.quantita = quantita;
    }

    public int getMinutiPreparazione() {
        return minutiPreparazione;
    }

    public List<WebCartItemCharacteristic> getCaratteristiche() {
        return new ArrayList<>(caratteristiche);
    }

    public List<Long> getIdCaratteristiche() {
        List<Long> ids = new ArrayList<>();
        for (WebCartItemCharacteristic caratteristica : caratteristiche) {
            ids.add(caratteristica.getIdCaratteristica());
        }
        return ids;
    }

    public BigDecimal getPrezzoUnitario() {
        BigDecimal prezzoUnitario = prezzoBase;
        for (WebCartItemCharacteristic caratteristica : caratteristiche) {
            prezzoUnitario = prezzoUnitario.add(caratteristica.getDifferenzaPrezzo());
        }
        return prezzoUnitario;
    }

    public BigDecimal getSubtotaleRiga() {
        return getPrezzoUnitario().multiply(BigDecimal.valueOf(quantita));
    }

    public int getMinutiPreparazioneTotali() {
        return minutiPreparazione * quantita;
    }

    void incrementaQuantita(int incremento) {
        if (incremento <= 0) {
            throw new IllegalArgumentException("La quantita deve essere maggiore di zero");
        }
        this.quantita += incremento;
    }
}
