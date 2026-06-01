package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.GruppoCaratteristicheDAO;
import it.univaq.mastereat.model.GruppoCaratteristiche;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GruppoCaratteristicheDAOImpl implements GruppoCaratteristicheDAO {

    private static final String SELECT_GRUPPO = """
            SELECT id,
                   id_prodotto,
                   nome,
                   descrizione,
                   obbligatorio,
                   attivo,
                   creato_il,
                   aggiornato_il
            FROM gruppi_caratteristiche
            """;

    @Override
    public List<GruppoCaratteristiche> findAllByProdottoIdForOwner(int idProdotto) throws SQLException {
        String sql = SELECT_GRUPPO + """
                WHERE id_prodotto = ?
                ORDER BY attivo DESC,
                         nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        }
    }

    @Override
    public List<GruppoCaratteristiche> findActiveByProdottoId(int idProdotto) throws SQLException {
        String sql = SELECT_GRUPPO + """
                WHERE id_prodotto = ?
                  AND attivo = TRUE
                ORDER BY nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        }
    }

    @Override
    public Optional<GruppoCaratteristiche> findByProdottoIdAndIdForOwner(int idProdotto,
                                                                         long idGruppoCaratteristiche)
            throws SQLException {
        String sql = SELECT_GRUPPO + """
                WHERE id_prodotto = ?
                  AND id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idGruppoCaratteristiche);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public boolean existsActiveByProdottoIdAndId(int idProdotto,
                                                 long idGruppoCaratteristiche) throws SQLException {
        String sql = """
                SELECT 1
                FROM gruppi_caratteristiche
                WHERE id_prodotto = ?
                  AND id = ?
                  AND attivo = TRUE
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idGruppoCaratteristiche);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean existsActiveByProdottoIdAndNome(int idProdotto,
                                                   String nome,
                                                   Long idGruppoDaEscludere) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT 1
                FROM gruppi_caratteristiche
                WHERE id_prodotto = ?
                  AND LOWER(nome) = LOWER(?)
                  AND attivo = TRUE
                """);
        if (idGruppoDaEscludere != null) {
            sql.append("  AND id <> ?\n");
        }
        sql.append("LIMIT 1");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            statement.setInt(1, idProdotto);
            statement.setString(2, nome);
            if (idGruppoDaEscludere != null) {
                statement.setLong(3, idGruppoDaEscludere);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean existsActiveCaratteristicheByGruppoForOwner(int idProdotto,
                                                               long idGruppoCaratteristiche) throws SQLException {
        String sql = """
                SELECT 1
                FROM caratteristiche
                WHERE id_prodotto = ?
                  AND id_gruppo_caratteristiche = ?
                  AND attiva = TRUE
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idGruppoCaratteristiche);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public GruppoCaratteristiche createForOwner(int idProdotto,
                                                String nome,
                                                String descrizione,
                                                boolean obbligatorio,
                                                boolean attivo) throws SQLException {
        String sql = """
                INSERT INTO gruppi_caratteristiche (
                    id_prodotto,
                    nome,
                    descrizione,
                    obbligatorio,
                    attivo
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, idProdotto);
            statement.setString(2, nome);
            statement.setString(3, descrizione);
            statement.setBoolean(4, obbligatorio);
            statement.setBoolean(5, attivo);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Nessun id generato per il nuovo gruppo caratteristiche");
                }

                return findByProdottoIdAndIdForOwner(idProdotto, generatedKeys.getLong(1))
                        .orElseThrow(() -> new SQLException("Gruppo caratteristiche creato non recuperabile"));
            }
        }
    }

    @Override
    public Optional<GruppoCaratteristiche> updateForOwner(int idProdotto,
                                                          long idGruppoCaratteristiche,
                                                          String nome,
                                                          String descrizione,
                                                          boolean obbligatorio,
                                                          boolean attivo) throws SQLException {
        String sql = """
                UPDATE gruppi_caratteristiche
                SET nome = ?,
                    descrizione = ?,
                    obbligatorio = ?,
                    attivo = ?
                WHERE id_prodotto = ?
                  AND id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nome);
            statement.setString(2, descrizione);
            statement.setBoolean(3, obbligatorio);
            statement.setBoolean(4, attivo);
            statement.setInt(5, idProdotto);
            statement.setLong(6, idGruppoCaratteristiche);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                return Optional.empty();
            }

            return findByProdottoIdAndIdForOwner(idProdotto, idGruppoCaratteristiche);
        }
    }

    @Override
    public boolean deactivateForOwner(int idProdotto, long idGruppoCaratteristiche) throws SQLException {
        String sql = """
                UPDATE gruppi_caratteristiche
                SET attivo = FALSE,
                    aggiornato_il = CURRENT_TIMESTAMP
                WHERE id_prodotto = ?
                  AND id = ?
                  AND attivo = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idGruppoCaratteristiche);

            return statement.executeUpdate() > 0;
        }
    }

    private List<GruppoCaratteristiche> mapRows(ResultSet resultSet) throws SQLException {
        List<GruppoCaratteristiche> gruppi = new ArrayList<>();
        while (resultSet.next()) {
            gruppi.add(mapRow(resultSet));
        }
        return gruppi;
    }

    private GruppoCaratteristiche mapRow(ResultSet resultSet) throws SQLException {
        return new GruppoCaratteristiche(
                resultSet.getLong("id"),
                resultSet.getLong("id_prodotto"),
                resultSet.getString("nome"),
                resultSet.getString("descrizione"),
                resultSet.getBoolean("obbligatorio"),
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
