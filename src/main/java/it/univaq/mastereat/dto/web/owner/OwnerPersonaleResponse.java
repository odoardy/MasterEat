package it.univaq.mastereat.dto.web.owner;

public class OwnerPersonaleResponse {

    private long id;
    private String username;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    private String creatoIl;

    public OwnerPersonaleResponse() {
    }

    public OwnerPersonaleResponse(long id,
                                  String username,
                                  String nome,
                                  String cognome,
                                  String email,
                                  String telefono,
                                  String creatoIl) {
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
        this.creatoIl = creatoIl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(String creatoIl) {
        this.creatoIl = creatoIl;
    }
}
