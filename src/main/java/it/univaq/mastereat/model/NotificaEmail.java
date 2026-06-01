package it.univaq.mastereat.model;

public class NotificaEmail {

    public enum Tipo {
        ORDINE_CONFERMATO,
        ORDINE_IN_CONSEGNA
    }

    public enum Stato {
        DA_INVIARE,
        INVIATA,
        FALLITA
    }

    private long id;
    private long idOrdine;
    private String emailDestinatario;
    private Tipo tipo;
    private String oggetto;
    private Stato stato;
    private String creataIl;
    private String inviataIl;
    private String messaggioErrore;

    public NotificaEmail() {
    }

    public NotificaEmail(long id,
                         long idOrdine,
                         String emailDestinatario,
                         Tipo tipo,
                         String oggetto,
                         Stato stato,
                         String creataIl,
                         String inviataIl,
                         String messaggioErrore) {
        this.id = id;
        this.idOrdine = idOrdine;
        this.emailDestinatario = emailDestinatario;
        this.tipo = tipo;
        this.oggetto = oggetto;
        this.stato = stato;
        this.creataIl = creataIl;
        this.inviataIl = inviataIl;
        this.messaggioErrore = messaggioErrore;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(long idOrdine) {
        this.idOrdine = idOrdine;
    }

    public String getEmailDestinatario() {
        return emailDestinatario;
    }

    public void setEmailDestinatario(String emailDestinatario) {
        this.emailDestinatario = emailDestinatario;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public Stato getStato() {
        return stato;
    }

    public void setStato(Stato stato) {
        this.stato = stato;
    }

    public String getCreataIl() {
        return creataIl;
    }

    public void setCreataIl(String creataIl) {
        this.creataIl = creataIl;
    }

    public String getInviataIl() {
        return inviataIl;
    }

    public void setInviataIl(String inviataIl) {
        this.inviataIl = inviataIl;
    }

    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }
}
