package it.univaq.mastereat.dto.api.ordini;

public class CambioStatoOperatoreResponse {

    private String statoPrecedente;
    private String statoNuovo;
    private String cambiatoIl;

    public CambioStatoOperatoreResponse() {
    }

    public CambioStatoOperatoreResponse(String statoPrecedente, String statoNuovo, String cambiatoIl) {
        this.statoPrecedente = statoPrecedente;
        this.statoNuovo = statoNuovo;
        this.cambiatoIl = cambiatoIl;
    }

    public String getStatoPrecedente() {
        return statoPrecedente;
    }

    public void setStatoPrecedente(String statoPrecedente) {
        this.statoPrecedente = statoPrecedente;
    }

    public String getStatoNuovo() {
        return statoNuovo;
    }

    public void setStatoNuovo(String statoNuovo) {
        this.statoNuovo = statoNuovo;
    }

    public String getCambiatoIl() {
        return cambiatoIl;
    }

    public void setCambiatoIl(String cambiatoIl) {
        this.cambiatoIl = cambiatoIl;
    }
}
