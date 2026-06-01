package it.univaq.mastereat.dto.common;

public class ImmagineProdottoPubblicaResponse {

    private long id;
    private String url;
    private String testoAlternativo;
    private boolean principale;

    public ImmagineProdottoPubblicaResponse() {
    }

    public ImmagineProdottoPubblicaResponse(long id,
                                            String url,
                                            String testoAlternativo,
                                            boolean principale) {
        this.id = id;
        this.url = url;
        this.testoAlternativo = testoAlternativo;
        this.principale = principale;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTestoAlternativo() {
        return testoAlternativo;
    }

    public void setTestoAlternativo(String testoAlternativo) {
        this.testoAlternativo = testoAlternativo;
    }

    public boolean isPrincipale() {
        return principale;
    }

    public void setPrincipale(boolean principale) {
        this.principale = principale;
    }
}
