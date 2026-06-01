package it.univaq.mastereat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class Caratteristica {

    private long id;
    private long idProdotto;
    private Long idGruppoCaratteristiche;
    private String nomeGruppoCaratteristiche;
    private String descrizioneGruppoCaratteristiche;
    private String nome;
    private String descrizione;
    private BigDecimal differenzaPrezzo;
    private boolean selezionataDefault;
    private boolean attiva;
    private String creatoIl;
    private String aggiornatoIl;

    public Caratteristica() {
    }

    public Caratteristica(long id,
                          long idProdotto,
                          Long idGruppoCaratteristiche,
                          String nomeGruppoCaratteristiche,
                          String descrizioneGruppoCaratteristiche,
                          String nome,
                          String descrizione,
                          BigDecimal differenzaPrezzo,
                          boolean selezionataDefault,
                          boolean attiva,
                          String creatoIl,
                          String aggiornatoIl) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.idGruppoCaratteristiche = idGruppoCaratteristiche;
        this.nomeGruppoCaratteristiche = nomeGruppoCaratteristiche;
        this.descrizioneGruppoCaratteristiche = descrizioneGruppoCaratteristiche;
        this.nome = nome;
        this.descrizione = descrizione;
        this.differenzaPrezzo = differenzaPrezzo;
        this.selezionataDefault = selezionataDefault;
        this.attiva = attiva;
        this.creatoIl = creatoIl;
        this.aggiornatoIl = aggiornatoIl;
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

    public Long getIdGruppoCaratteristiche() {
        return idGruppoCaratteristiche;
    }

    public void setIdGruppoCaratteristiche(Long idGruppoCaratteristiche) {
        this.idGruppoCaratteristiche = idGruppoCaratteristiche;
    }

    @JsonIgnore
    public String getNomeGruppoCaratteristiche() {
        return nomeGruppoCaratteristiche;
    }

    public void setNomeGruppoCaratteristiche(String nomeGruppoCaratteristiche) {
        this.nomeGruppoCaratteristiche = nomeGruppoCaratteristiche;
    }

    @JsonIgnore
    public String getDescrizioneGruppoCaratteristiche() {
        return descrizioneGruppoCaratteristiche;
    }

    public void setDescrizioneGruppoCaratteristiche(String descrizioneGruppoCaratteristiche) {
        this.descrizioneGruppoCaratteristiche = descrizioneGruppoCaratteristiche;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public BigDecimal getDifferenzaPrezzo() {
        return differenzaPrezzo;
    }

    public void setDifferenzaPrezzo(BigDecimal differenzaPrezzo) {
        this.differenzaPrezzo = differenzaPrezzo;
    }

    public boolean isSelezionataDefault() {
        return selezionataDefault;
    }

    public void setSelezionataDefault(boolean selezionataDefault) {
        this.selezionataDefault = selezionataDefault;
    }

    public boolean isAttiva() {
        return attiva;
    }

    public void setAttiva(boolean attiva) {
        this.attiva = attiva;
    }

    public String getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(String creatoIl) {
        this.creatoIl = creatoIl;
    }

    public String getAggiornatoIl() {
        return aggiornatoIl;
    }

    public void setAggiornatoIl(String aggiornatoIl) {
        this.aggiornatoIl = aggiornatoIl;
    }
}
