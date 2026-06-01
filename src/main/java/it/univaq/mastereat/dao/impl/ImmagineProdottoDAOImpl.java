package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.ImmagineProdottoDAO;
import it.univaq.mastereat.model.ImmagineProdotto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImmagineProdottoDAOImpl implements ImmagineProdottoDAO {

    private static final String SELECT_IMMAGINE = """
            SELECT id,
                   id_prodotto,
                   nome_file_originale,
                   nome_file_salvato,
                   percorso_file,
                   tipo_contenuto,
                   dimensione_byte,
                   testo_alternativo,
                   ordine_visualizzazione,
                   principale,
                   caricata_il
            FROM immagini_prodotto
            """;

    @Override
    public List<ImmagineProdotto> findByProdottoId(int idProdotto) throws SQLException {
        Map<Long, List<ImmagineProdotto>> immaginiByProdottoId = findByProdottoIds(List.of((long) idProdotto));
        return immaginiByProdottoId.getOrDefault((long) idProdotto, Collections.emptyList());
    }

    @Override
    public Map<Long, List<ImmagineProdotto>> findByProdottoIds(List<Long> idProdotti) throws SQLException {
        if (idProdotti == null || idProdotti.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = idProdotti.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = SELECT_IMMAGINE + """
                WHERE id_prodotto IN (
                """ + placeholders + """
                )
                ORDER BY id_prodotto,
                         principale DESC,
                         ordine_visualizzazione,
                         id
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int index = 0; index < idProdotti.size(); index++) {
                statement.setLong(index + 1, idProdotti.get(index));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Long, List<ImmagineProdotto>> immaginiByProdottoId = new LinkedHashMap<>();
                while (resultSet.next()) {
                    ImmagineProdotto immagine = mapRow(resultSet);
                    immaginiByProdottoId
                            .computeIfAbsent(immagine.getIdProdotto(), id -> new ArrayList<>())
                            .add(immagine);
                }
                return immaginiByProdottoId;
            }
        }
    }

    @Override
    public Optional<ImmagineProdotto> findByProdottoIdAndId(int idProdotto, long idImmagine) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            return findByProdottoIdAndId(connection, idProdotto, idImmagine);
        }
    }

    @Override
    public long countByProdottoId(int idProdotto) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS totale
                FROM immagini_prodotto
                WHERE id_prodotto = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong("totale") : 0;
            }
        }
    }

    @Override
    public ImmagineProdotto create(int idProdotto,
                                   String nomeFileOriginale,
                                   String nomeFileSalvato,
                                   String percorsoFile,
                                   String tipoContenuto,
                                   long dimensioneByte,
                                   String testoAlternativo,
                                   int ordineVisualizzazione,
                                   boolean principale) throws SQLException {
        String sql = """
                INSERT INTO immagini_prodotto (
                    id_prodotto,
                    nome_file_originale,
                    nome_file_salvato,
                    percorso_file,
                    tipo_contenuto,
                    dimensione_byte,
                    testo_alternativo,
                    ordine_visualizzazione,
                    principale
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (principale) {
                    resetPrincipale(connection, idProdotto);
                }

                statement.setInt(1, idProdotto);
                statement.setString(2, nomeFileOriginale);
                statement.setString(3, nomeFileSalvato);
                statement.setString(4, percorsoFile);
                statement.setString(5, tipoContenuto);
                statement.setLong(6, dimensioneByte);
                statement.setString(7, testoAlternativo);
                statement.setInt(8, ordineVisualizzazione);
                statement.setBoolean(9, principale);
                statement.executeUpdate();

                long idImmagine;
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Nessun id generato per la nuova immagine prodotto");
                    }
                    idImmagine = generatedKeys.getLong(1);
                }

                ImmagineProdotto immagine = findByProdottoIdAndId(connection, idProdotto, idImmagine)
                        .orElseThrow(() -> new SQLException("Immagine prodotto creata non recuperabile"));
                connection.commit();
                return immagine;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    @Override
    public boolean setPrincipale(int idProdotto, long idImmagine) throws SQLException {
        String sql = """
                UPDATE immagini_prodotto
                SET principale = TRUE
                WHERE id_prodotto = ?
                  AND id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                resetPrincipale(connection, idProdotto);

                statement.setInt(1, idProdotto);
                statement.setLong(2, idImmagine);
                int updatedRows = statement.executeUpdate();
                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    @Override
    public Optional<ImmagineProdotto> deleteByProdottoIdAndId(int idProdotto, long idImmagine) throws SQLException {
        String sql = """
                DELETE FROM immagini_prodotto
                WHERE id_prodotto = ?
                  AND id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                Optional<ImmagineProdotto> immagine = findByProdottoIdAndId(connection, idProdotto, idImmagine);
                if (immagine.isEmpty()) {
                    connection.rollback();
                    return Optional.empty();
                }

                statement.setInt(1, idProdotto);
                statement.setLong(2, idImmagine);
                statement.executeUpdate();

                if (immagine.get().isPrincipale()) {
                    promuoviPrimaDisponibile(connection, idProdotto);
                }

                connection.commit();
                return immagine;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private Optional<ImmagineProdotto> findByProdottoIdAndId(Connection connection,
                                                            int idProdotto,
                                                            long idImmagine) throws SQLException {
        String sql = SELECT_IMMAGINE + """
                WHERE id_prodotto = ?
                  AND id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProdotto);
            statement.setLong(2, idImmagine);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    private void resetPrincipale(Connection connection, int idProdotto) throws SQLException {
        String sql = """
                UPDATE immagini_prodotto
                SET principale = FALSE
                WHERE id_prodotto = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProdotto);
            statement.executeUpdate();
        }
    }

    private void promuoviPrimaDisponibile(Connection connection, int idProdotto) throws SQLException {
        String selectSql = """
                SELECT id
                FROM immagini_prodotto
                WHERE id_prodotto = ?
                ORDER BY ordine_visualizzazione,
                         id
                LIMIT 1
                """;
        String updateSql = """
                UPDATE immagini_prodotto
                SET principale = TRUE
                WHERE id_prodotto = ?
                  AND id = ?
                """;

        resetPrincipale(connection, idProdotto);

        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setInt(1, idProdotto);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return;
                }

                long idImmagine = resultSet.getLong("id");
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                    updateStatement.setInt(1, idProdotto);
                    updateStatement.setLong(2, idImmagine);
                    updateStatement.executeUpdate();
                }
            }
        }
    }

    private ImmagineProdotto mapRow(ResultSet resultSet) throws SQLException {
        return new ImmagineProdotto(
                resultSet.getLong("id"),
                resultSet.getLong("id_prodotto"),
                resultSet.getString("nome_file_originale"),
                resultSet.getString("nome_file_salvato"),
                resultSet.getString("percorso_file"),
                resultSet.getString("tipo_contenuto"),
                resultSet.getLong("dimensione_byte"),
                resultSet.getString("testo_alternativo"),
                resultSet.getInt("ordine_visualizzazione"),
                resultSet.getBoolean("principale"),
                getTimestampAsString(resultSet, "caricata_il")
        );
    }

    private String getTimestampAsString(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime().toString() : null;
    }
}
