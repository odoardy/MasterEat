package it.univaq.mastereat.model;

import java.util.ArrayList;
import java.util.List;

public class RigaOrdineDaCreare {

    private final Prodotto prodotto;
    private final int quantita;
    private final List<Caratteristica> caratteristiche;

    public RigaOrdineDaCreare(Prodotto prodotto, int quantita, List<Caratteristica> caratteristiche) {
        this.prodotto = prodotto;
        this.quantita = quantita;
        this.caratteristiche = caratteristiche != null ? new ArrayList<>(caratteristiche) : new ArrayList<>();
    }

    public Prodotto getProdotto() {
        return prodotto;
    }

    public int getQuantita() {
        return quantita;
    }

    public List<Caratteristica> getCaratteristiche() {
        return new ArrayList<>(caratteristiche);
    }
}
