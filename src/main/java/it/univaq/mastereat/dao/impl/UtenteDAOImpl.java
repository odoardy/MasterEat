package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.model.UtentePasswordHash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtenteDAOImpl implements UtenteDAO {

    private static final String SELECT_UTENTE = """
            SELECT id,
                   username,
                   email,
                   nome,
                   cognome,
                   telefono,
                   indirizzo,
                   citta,
                   cap,
                   ruolo,
                   attivo,
                   creato_il,
                   aggiornato_il
            FROM utenti
            """;

    @Override
    public Optional<Utente> findByUsernameAndPasswordHash(String username, String passwordHash) throws SQLException {
        String sql = SELECT_UTENTE + """
                WHERE username = ?
                  AND password_hash = ?
                  AND attivo = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, passwordHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<UtentePasswordHash> findActiveByUsernameWithPasswordHash(String username) throws SQLException {
        String sql = """
                SELECT id,
                       username,
                       email,
                       password_hash,
                       nome,
                       cognome,
                       telefono,
                       indirizzo,
                       citta,
                       cap,
                       ruolo,
                       attivo,
                       creato_il,
                       aggiornato_il
                FROM utenti
                WHERE username = ?
                  AND attivo = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new UtentePasswordHash(
                            mapRow(resultSet),
                            resultSet.getString("password_hash")
                    ));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Utente> findByUsername(String username) throws SQLException {
        String sql = SELECT_UTENTE + """
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Utente> findById(int id) throws SQLException {
        String sql = SELECT_UTENTE + """
                WHERE id = ?
                  AND attivo = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        String sql = """
                SELECT 1
                FROM utenti
                WHERE username = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = """
                SELECT 1
                FROM utenti
                WHERE email = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean existsByEmailForOtherUser(String email, long idUtente) throws SQLException {
        String sql = """
                SELECT 1
                FROM utenti
                WHERE email = ?
                  AND id <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setLong(2, idUtente);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public List<Utente> findByRole(String ruolo) throws SQLException {
        String sql = SELECT_UTENTE + """
                WHERE ruolo = ?
                  AND attivo = TRUE
                ORDER BY creato_il DESC, id DESC
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, ruolo);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Utente> utenti = new ArrayList<>();
                while (resultSet.next()) {
                    utenti.add(mapRow(resultSet));
                }
                return utenti;
            }
        }
    }

    @Override
    public Utente createCliente(String username,
                                String email,
                                String passwordHash,
                                String nome,
                                String cognome,
                                String telefono,
                                String indirizzo,
                                String citta,
                                String cap) throws SQLException {
        String sql = """
                INSERT INTO utenti (
                    username,
                    email,
                    password_hash,
                    nome,
                    cognome,
                    telefono,
                    indirizzo,
                    citta,
                    cap,
                    ruolo,
                    attivo
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'CLIENTE', TRUE)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, nome);
            statement.setString(5, cognome);
            statement.setString(6, telefono);
            statement.setString(7, indirizzo);
            statement.setString(8, citta);
            statement.setString(9, cap);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Nessun id generato per il nuovo cliente");
                }

                return findById(generatedKeys.getInt(1))
                        .orElseThrow(() -> new SQLException("Cliente creato non recuperabile"));
            }
        }
    }

    @Override
    public Utente createPersonale(String username,
                                  String email,
                                  String passwordHash,
                                  String nome,
                                  String cognome,
                                  String telefono) throws SQLException {
        String sql = """
                INSERT INTO utenti (
                    username,
                    email,
                    password_hash,
                    nome,
                    cognome,
                    telefono,
                    ruolo,
                    attivo
                ) VALUES (?, ?, ?, ?, ?, ?, 'PERSONALE', TRUE)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, nome);
            statement.setString(5, cognome);
            statement.setString(6, telefono);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Nessun id generato per il nuovo membro personale");
                }

                return findById(generatedKeys.getInt(1))
                        .orElseThrow(() -> new SQLException("Membro personale creato non recuperabile"));
            }
        }
    }

    @Override
    public Optional<Utente> updateClienteProfile(long idUtente,
                                                 String nome,
                                                 String cognome,
                                                 String email,
                                                 String telefono,
                                                 String indirizzo,
                                                 String citta,
                                                 String cap) throws SQLException {
        String sql = """
                UPDATE utenti
                SET nome = ?,
                    cognome = ?,
                    email = ?,
                    telefono = ?,
                    indirizzo = ?,
                    citta = ?,
                    cap = ?
                WHERE id = ?
                  AND ruolo = 'CLIENTE'
                  AND attivo = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nome);
            statement.setString(2, cognome);
            statement.setString(3, email);
            statement.setString(4, telefono);
            statement.setString(5, indirizzo);
            statement.setString(6, citta);
            statement.setString(7, cap);
            statement.setLong(8, idUtente);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                return Optional.empty();
            }

            return findById(Math.toIntExact(idUtente));
        }
    }

    @Override
    public boolean updatePasswordHash(long idUtente, String passwordHash) throws SQLException {
        String sql = """
                UPDATE utenti
                SET password_hash = ?
                WHERE id = ?
                  AND attivo = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setLong(2, idUtente);

            return statement.executeUpdate() > 0;
        }
    }

    private Utente mapRow(ResultSet resultSet) throws SQLException {
        return new Utente(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getString("nome"),
                resultSet.getString("cognome"),
                resultSet.getString("telefono"),
                resultSet.getString("indirizzo"),
                resultSet.getString("citta"),
                resultSet.getString("cap"),
                resultSet.getString("ruolo"),
                resultSet.getBoolean("attivo"),
                getTimestampAsString(resultSet, "creato_il"),
                getTimestampAsString(resultSet, "aggiornato_il")
        );
    }

    private String getTimestampAsString(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime().toString() : null;
    }
}
