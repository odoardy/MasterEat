package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.SessioneApi;

import java.sql.SQLException;
import java.util.Optional;

public interface SessioneApiDAO {

    String createSession(int idUtente) throws SQLException;

    boolean invalidateSession(String token) throws SQLException;

    Optional<SessioneApi> findByToken(String token) throws SQLException;
}
