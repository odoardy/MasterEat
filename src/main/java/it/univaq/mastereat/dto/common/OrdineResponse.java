package it.univaq.mastereat.dto.common;

import java.math.BigDecimal;

public class OrdineResponse {

    private long id;
    private long idCliente;
    private String stato;
    private BigDecimal prezzoTotale;
    private Integer minutiConsegnaStimati;
    private String indirizzoConsegnaSnapshot;
    private String cittaConsegnaSnapshot;
    private String capConsegnaSnapshot;
    private String telefonoConsegnaSnapshot;
    private String creatoIl;
    private String confermatoIl;
    private String annullatoIl;

    public OrdineResponse() {
    }

    public OrdineResponse(long id,
                          long idCliente,
                          String stato,
                          BigDecimal prezzoTotale,
                          Integer minutiConsegnaStimati,
                          String indirizzoConsegnaSnapshot,
                          String cittaConsegnaSnapshot,
                          String capConsegnaSnapshot,
                          String telefonoConsegnaSnapshot,
                          String creatoIl,
                          String confermatoIl,
                          String annullatoIl) {
        this.id = id;
        this.idCliente = idCliente;
        this.stato = stato;
        this.prezzoTotale = prezzoTotale;
        this.minutiConsegnaStimati = minutiConsegnaStimati;
        this.indirizzoConsegnaSnapshot = indirizzoConsegnaSnapshot;
        this.cittaConsegnaSnapshot = cittaConsegnaSnapshot;
        this.capConsegnaSnapshot = capConsegnaSnapshot;
        this.telefonoConsegnaSnapshot = telefonoConsegnaSnapshot;
        this.creatoIl = creatoIl;
        this.confermatoIl = confermatoIl;
        this.annullatoIl = annullatoIl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public BigDecimal getPrezzoTotale() {
        return prezzoTotale;
    }

    public void setPrezzoTotale(BigDecimal prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }

    public Integer getMinutiConsegnaStimati() {
        return minutiConsegnaStimati;
    }

    public void setMinutiConsegnaStimati(Integer minutiConsegnaStimati) {
        this.minutiConsegnaStimati = minutiConsegnaStimati;
    }

    public String getIndirizzoConsegnaSnapshot() {
        return indirizzoConsegnaSnapshot;
    }

    public void setIndirizzoConsegnaSnapshot(String indirizzoConsegnaSnapshot) {
        this.indirizzoConsegnaSnapshot = indirizzoConsegnaSnapshot;
    }

    public String getCittaConsegnaSnapshot() {
        return cittaConsegnaSnapshot;
    }

    public void setCittaConsegnaSnapshot(String cittaConsegnaSnapshot) {
        this.cittaConsegnaSnapshot = cittaConsegnaSnapshot;
    }

    public String getCapConsegnaSnapshot() {
        return capConsegnaSnapshot;
    }

    public void setCapConsegnaSnapshot(String capConsegnaSnapshot) {
        this.capConsegnaSnapshot = capConsegnaSnapshot;
    }

    public String getTelefonoConsegnaSnapshot() {
        return telefonoConsegnaSnapshot;
    }

    public void setTelefonoConsegnaSnapshot(String telefonoConsegnaSnapshot) {
        this.telefonoConsegnaSnapshot = telefonoConsegnaSnapshot;
    }

    public String getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(String creatoIl) {
        this.creatoIl = creatoIl;
    }

    public String getConfermatoIl() {
        return confermatoIl;
    }

    public void setConfermatoIl(String confermatoIl) {
        this.confermatoIl = confermatoIl;
    }

    public String getAnnullatoIl() {
        return annullatoIl;
    }

    public void setAnnullatoIl(String annullatoIl) {
        this.annullatoIl = annullatoIl;
    }
}
