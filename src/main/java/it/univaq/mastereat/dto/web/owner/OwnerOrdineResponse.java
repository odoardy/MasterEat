package it.univaq.mastereat.dto.web.owner;

import java.math.BigDecimal;

public class OwnerOrdineResponse {

    private long id;
    private long idCliente;
    private String cliente;
    private String usernameCliente;
    private String stato;
    private BigDecimal prezzoTotale;
    private Integer minutiConsegnaStimati;
    private String creatoIl;
    private String confermatoIl;
    private String orarioConsegnaRichiesto;
    private String indirizzoConsegnaSnapshot;
    private String cittaConsegnaSnapshot;
    private String capConsegnaSnapshot;
    private String telefonoConsegnaSnapshot;
    private int numeroProdotti;
    private String operatoriRiepilogo;

    public OwnerOrdineResponse() {
    }

    public OwnerOrdineResponse(long id,
                               long idCliente,
                               String cliente,
                               String usernameCliente,
                               String stato,
                               BigDecimal prezzoTotale,
                               Integer minutiConsegnaStimati,
                               String creatoIl,
                               String confermatoIl,
                               String orarioConsegnaRichiesto,
                               String indirizzoConsegnaSnapshot,
                               String cittaConsegnaSnapshot,
                               String capConsegnaSnapshot,
                               String telefonoConsegnaSnapshot,
                               int numeroProdotti,
                               String operatoriRiepilogo) {
        this.id = id;
        this.idCliente = idCliente;
        this.cliente = cliente;
        this.usernameCliente = usernameCliente;
        this.stato = stato;
        this.prezzoTotale = prezzoTotale;
        this.minutiConsegnaStimati = minutiConsegnaStimati;
        this.creatoIl = creatoIl;
        this.confermatoIl = confermatoIl;
        this.orarioConsegnaRichiesto = orarioConsegnaRichiesto;
        this.indirizzoConsegnaSnapshot = indirizzoConsegnaSnapshot;
        this.cittaConsegnaSnapshot = cittaConsegnaSnapshot;
        this.capConsegnaSnapshot = capConsegnaSnapshot;
        this.telefonoConsegnaSnapshot = telefonoConsegnaSnapshot;
        this.numeroProdotti = numeroProdotti;
        this.operatoriRiepilogo = operatoriRiepilogo;
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

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getUsernameCliente() {
        return usernameCliente;
    }

    public void setUsernameCliente(String usernameCliente) {
        this.usernameCliente = usernameCliente;
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

    public String getDataInserimento() {
        if (confermatoIl != null && !confermatoIl.isBlank()) {
            return confermatoIl;
        }
        return creatoIl;
    }

    public String getOrarioConsegnaRichiesto() {
        return orarioConsegnaRichiesto;
    }

    public void setOrarioConsegnaRichiesto(String orarioConsegnaRichiesto) {
        this.orarioConsegnaRichiesto = orarioConsegnaRichiesto;
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

    public int getNumeroProdotti() {
        return numeroProdotti;
    }

    public void setNumeroProdotti(int numeroProdotti) {
        this.numeroProdotti = numeroProdotti;
    }

    public String getOperatoriRiepilogo() {
        return operatoriRiepilogo;
    }

    public void setOperatoriRiepilogo(String operatoriRiepilogo) {
        this.operatoriRiepilogo = operatoriRiepilogo;
    }
}
