package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.model.UtentePasswordHash;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UtenteDAO {

    Optional<Utente> findByUsernameAndPasswordHash(String username, String passwordHash) throws SQLException;

    Optional<UtentePasswordHash> findActiveByUsernameWithPasswordHash(String username) throws SQLException;

    Optional<Utente> findByUsername(String username) throws SQLException;

    Optional<Utente> findById(int id) throws SQLException;

    boolean existsByUsername(String username) throws SQLException;

    boolean existsByEmail(String email) throws SQLException;

    boolean existsByEmailForOtherUser(String email, long idUtente) throws SQLException;

    List<Utente> findByRole(String ruolo) throws SQLException;

    Utente createCliente(String username,
                         String email,
                         String passwordHash,
                         String nome,
                         String cognome,
                         String telefono,
                         String indirizzo,
                         String citta,
                         String cap) throws SQLException;

    Utente createPersonale(String username,
                           String email,
                           String passwordHash,
                           String nome,
                           String cognome,
                           String telefono) throws SQLException;

    Optional<Utente> updateClienteProfile(long idUtente,
                                          String nome,
                                          String cognome,
                                          String email,
                                          String telefono,
                                          String indirizzo,
                                          String citta,
                                          String cap) throws SQLException;

    boolean updatePasswordHash(long idUtente, String passwordHash) throws SQLException;
}
