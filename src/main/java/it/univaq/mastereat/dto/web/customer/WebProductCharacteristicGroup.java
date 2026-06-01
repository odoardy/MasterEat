package it.univaq.mastereat.dto.web.customer;

import it.univaq.mastereat.model.Caratteristica;

import java.util.ArrayList;
import java.util.List;

public class WebProductCharacteristicGroup {

    private final long idGruppo;
    private final String nomeGruppo;
    private final String descrizioneGruppo;
    private final List<Caratteristica> caratteristiche;

    public WebProductCharacteristicGroup(long idGruppo,
                                         String nomeGruppo,
                                         String descrizioneGruppo,
                                         List<Caratteristica> caratteristiche) {
        this.idGruppo = idGruppo;
        this.nomeGruppo = nomeGruppo;
        this.descrizioneGruppo = descrizioneGruppo;
        this.caratteristiche = caratteristiche != null ? new ArrayList<>(caratteristiche) : new ArrayList<>();
    }

    public long getIdGruppo() {
        return idGruppo;
    }

    public String getNomeGruppo() {
        return nomeGruppo;
    }

    public String getDescrizioneGruppo() {
        return descrizioneGruppo;
    }

    public List<Caratteristica> getCaratteristiche() {
        return new ArrayList<>(caratteristiche);
    }
}
