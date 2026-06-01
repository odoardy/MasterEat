package it.univaq.mastereat.dto.web.owner;

public class OwnerPersonaleCreateRequest {

    private String username;
    private String password;
    private String confermaPassword;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;

    public OwnerPersonaleCreateRequest() {
    }

    public OwnerPersonaleCreateRequest(String username,
                                       String password,
                                       String confermaPassword,
                                       String nome,
                                       String cognome,
                                       String email,
                                       String telefono) {
        this.username = username;
        this.password = password;
        this.confermaPassword = confermaPassword;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfermaPassword() {
        return confermaPassword;
    }

    public void setConfermaPassword(String confermaPassword) {
        this.confermaPassword = confermaPassword;
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
}
