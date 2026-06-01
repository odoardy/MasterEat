package it.univaq.mastereat.model;

import java.math.BigDecimal;

public class Ordine {

    private long id;
    private long idCliente;
    private StatoOrdine stato;
    private String creatoIl;
    private String confermatoIl;
    private String orarioConsegnaRichiesto;
    private Integer minutiConsegnaStimati;
    private BigDecimal distanzaConsegnaKm;
    private BigDecimal prezzoTotale;
    private String indirizzoConsegnaSnapshot;
    private String cittaConsegnaSnapshot;
    private String capConsegnaSnapshot;
    private String telefonoConsegnaSnapshot;
    private String annullatoIl;
    private String motivoAnnullamento;

    public Ordine() {
    }

    public Ordine(long id,
                  long idCliente,
                  StatoOrdine stato,
                  String creatoIl,
                  String confermatoIl,
                  String orarioConsegnaRichiesto,
                  Integer minutiConsegnaStimati,
                  BigDecimal distanzaConsegnaKm,
                  BigDecimal prezzoTotale,
                  String indirizzoConsegnaSnapshot,
                  String cittaConsegnaSnapshot,
                  String capConsegnaSnapshot,
                  String telefonoConsegnaSnapshot,
                  String annullatoIl,
                  String motivoAnnullamento) {
        this.id = id;
        this.idCliente = idCliente;
        this.stato = stato;
        this.creatoIl = creatoIl;
        this.confermatoIl = confermatoIl;
        this.orarioConsegnaRichiesto = orarioConsegnaRichiesto;
        this.minutiConsegnaStimati = minutiConsegnaStimati;
        this.distanzaConsegnaKm = distanzaConsegnaKm;
        this.prezzoTotale = prezzoTotale;
        this.indirizzoConsegnaSnapshot = indirizzoConsegnaSnapshot;
        this.cittaConsegnaSnapshot = cittaConsegnaSnapshot;
        this.capConsegnaSnapshot = capConsegnaSnapshot;
        this.telefonoConsegnaSnapshot = telefonoConsegnaSnapshot;
        this.annullatoIl = annullatoIl;
        this.motivoAnnullamento = motivoAnnullamento;
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

    public StatoOrdine getStato() {
        return stato;
    }

    public void setStato(StatoOrdine stato) {
        this.stato = stato;
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

    public String getOrarioConsegnaRichiesto() {
        return orarioConsegnaRichiesto;
    }

    public void setOrarioConsegnaRichiesto(String orarioConsegnaRichiesto) {
        this.orarioConsegnaRichiesto = orarioConsegnaRichiesto;
    }

    public Integer getMinutiConsegnaStimati() {
        return minutiConsegnaStimati;
    }

    public void setMinutiConsegnaStimati(Integer minutiConsegnaStimati) {
        this.minutiConsegnaStimati = minutiConsegnaStimati;
    }

    public BigDecimal getDistanzaConsegnaKm() {
        return distanzaConsegnaKm;
    }

    public void setDistanzaConsegnaKm(BigDecimal distanzaConsegnaKm) {
        this.distanzaConsegnaKm = distanzaConsegnaKm;
    }

    public BigDecimal getPrezzoTotale() {
        return prezzoTotale;
    }

    public void setPrezzoTotale(BigDecimal prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
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

    public String getAnnullatoIl() {
        return annullatoIl;
    }

    public void setAnnullatoIl(String annullatoIl) {
        this.annullatoIl = annullatoIl;
    }

    public String getMotivoAnnullamento() {
        return motivoAnnullamento;
    }

    public void setMotivoAnnullamento(String motivoAnnullamento) {
        this.motivoAnnullamento = motivoAnnullamento;
    }
}
