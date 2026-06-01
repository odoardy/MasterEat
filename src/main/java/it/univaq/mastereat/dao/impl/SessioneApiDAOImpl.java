package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.SessioneApiDAO;
import it.univaq.mastereat.model.SessioneApi;
import it.univaq.mastereat.util.PasswordUtils;
import it.univaq.mastereat.util.TokenUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class SessioneApiDAOImpl implements SessioneApiDAO {

    private static final int DURATA_SESSIONE_ORE = 24;

    @Override
    public String createSession(int idUtente) throws SQLException {
        String token = TokenUtils.generateToken();
        String sql = """
                INSERT INTO sessioni_api (id_utente, token_hash, scade_il)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUtente);
            statement.setString(2, PasswordUtils.sha256(token));
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusHours(DURATA_SESSIONE_ORE)));
            statement.executeUpdate();

            return token;
        }
    }

    @Override
    public boolean invalidateSession(String token) throws SQLException {
        String sql = """
                UPDATE sessioni_api
                SET revocato_il = CURRENT_TIMESTAMP
                WHERE token_hash = ?
                  AND revocato_il IS NULL
                  AND scade_il > CURRENT_TIMESTAMP
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PasswordUtils.sha256(token));

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<SessioneApi> findByToken(String token) throws SQLException {
        String sql = """
                SELECT id,
                       id_utente,
                       token_hash,
                       creato_il,
                       scade_il,
                       revocato_il
                FROM sessioni_api
                WHERE token_hash = ?
                  AND revocato_il IS NULL
                  AND scade_il > CURRENT_TIMESTAMP
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PasswordUtils.sha256(token));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    private SessioneApi mapRow(ResultSet resultSet) throws SQLException {
        return new SessioneApi(
                resultSet.getLong("id"),
                resultSet.getLong("id_utente"),
                resultSet.getString("token_hash"),
                getTimestampAsString(resultSet, "creato_il"),
                getTimestampAsString(resultSet, "scade_il"),
                getTimestampAsString(resultSet, "revocato_il")
        );
    }

    private String getTimestampAsString(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime().toString() : null;
    }
}
