package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.NotificaEmailDAO;
import it.univaq.mastereat.model.NotificaEmail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

public class NotificaEmailDAOImpl implements NotificaEmailDAO {

    private static final int MAX_MESSAGGIO_ERRORE_LENGTH = 4000;

    private static final String SELECT_NOTIFICA = """
            SELECT id,
                   id_ordine,
                   email_destinatario,
                   tipo,
                   oggetto,
                   stato,
                   creata_il,
                   inviata_il,
                   messaggio_errore
            FROM notifiche_email
            """;

    @Override
    public boolean existsDaInviareOInviata(long idOrdine, NotificaEmail.Tipo tipo) throws SQLException {
        String sql = """
                SELECT 1
                FROM notifiche_email
                WHERE id_ordine = ?
                  AND tipo = ?
                  AND stato IN ('DA_INVIARE', 'INVIATA')
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idOrdine);
            statement.setString(2, tipo.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public NotificaEmail createDaInviare(long idOrdine,
                                         String emailDestinatario,
                                         NotificaEmail.Tipo tipo,
                                         String oggetto) throws SQLException {
        String sql = """
                INSERT INTO notifiche_email (
                    id_ordine,
                    email_destinatario,
                    tipo,
                    oggetto,
                    stato
                ) VALUES (?, ?, ?, ?, 'DA_INVIARE')
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, idOrdine);
            statement.setString(2, emailDestinatario);
            statement.setString(3, tipo.name());
            statement.setString(4, oggetto);
            statement.executeUpdate();

            long idNotifica = getGeneratedId(statement);
            return findById(connection, idNotifica)
                    .orElseThrow(() -> new SQLException("Notifica email creata non recuperabile"));
        }
    }

    @Override
    public void marcaInviata(long idNotifica) throws SQLException {
        String sql = """
                UPDATE notifiche_email
                SET stato = 'INVIATA',
                    inviata_il = CURRENT_TIMESTAMP,
                    messaggio_errore = NULL
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idNotifica);
            statement.executeUpdate();
        }
    }

    @Override
    public void marcaFallita(long idNotifica, String messaggioErrore) throws SQLException {
        String sql = """
                UPDATE notifiche_email
                SET stato = 'FALLITA',
                    inviata_il = NULL,
                    messaggio_errore = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, truncate(messaggioErrore, MAX_MESSAGGIO_ERRORE_LENGTH));
            statement.setLong(2, idNotifica);
            statement.executeUpdate();
        }
    }

    private Optional<NotificaEmail> findById(Connection connection, long idNotifica) throws SQLException {
        String sql = SELECT_NOTIFICA + """
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idNotifica);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    private NotificaEmail mapRow(ResultSet resultSet) throws SQLException {
        return new NotificaEmail(
                resultSet.getLong("id"),
                resultSet.getLong("id_ordine"),
                resultSet.getString("email_destinatario"),
                NotificaEmail.Tipo.valueOf(resultSet.getString("tipo")),
                resultSet.getString("oggetto"),
                NotificaEmail.Stato.valueOf(resultSet.getString("stato")),
                getTimestampAsString(resultSet, "creata_il"),
                getTimestampAsString(resultSet, "inviata_il"),
                resultSet.getString("messaggio_errore")
        );
    }

    private long getGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
        }

        throw new SQLException("Nessun id generato per la notifica email");
    }

    private String getTimestampAsString(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime().toString() : null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
