package it.univaq.mastereat.model;

import java.math.BigDecimal;

public class Prodotto {

    private long id;
    private Long idCategoria;
    private String nome;
    private String descrizione;
    private BigDecimal prezzoBase;
    private int minutiPreparazione;
    private String descrizionePreparazione;
    private boolean attivo;
    private String creatoIl;
    private String aggiornatoIl;

    public Prodotto() {
    }

    public Prodotto(long id,
                    Long idCategoria,
                    String nome,
                    String descrizione,
                    BigDecimal prezzoBase,
                    int minutiPreparazione,
                    String descrizionePreparazione,
                    boolean attivo,
                    String creatoIl,
                    String aggiornatoIl) {
        this.id = id;
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        this.minutiPreparazione = minutiPreparazione;
        this.descrizionePreparazione = descrizionePreparazione;
        this.attivo = attivo;
        this.creatoIl = creatoIl;
        this.aggiornatoIl = aggiornatoIl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
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

    public BigDecimal getPrezzoBase() {
        return prezzoBase;
    }

    public void setPrezzoBase(BigDecimal prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public int getMinutiPreparazione() {
        return minutiPreparazione;
    }

    public void setMinutiPreparazione(int minutiPreparazione) {
        this.minutiPreparazione = minutiPreparazione;
    }

    public String getDescrizionePreparazione() {
        return descrizionePreparazione;
    }

    public void setDescrizionePreparazione(String descrizionePreparazione) {
        this.descrizionePreparazione = descrizionePreparazione;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
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
