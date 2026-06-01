package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.IngredienteDAO;
import it.univaq.mastereat.model.Ingrediente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredienteDAOImpl implements IngredienteDAO {

    private static final String SELECT_INGREDIENTE_ASSOCIATO = """
            SELECT i.id,
                   ip.id_prodotto,
                   i.nome,
                   i.unita_misura,
                   ip.quantita,
                   i.allergene,
                   i.attivo,
                   i.creato_il,
                   i.aggiornato_il
            FROM ingredienti_prodotto ip
            JOIN ingredienti i ON i.id = ip.id_ingrediente
            """;

    private static final String SELECT_INGREDIENTE_GLOBALE = """
            SELECT i.id,
                   0 AS id_prodotto,
                   i.nome,
                   i.unita_misura,
                   NULL AS quantita,
                   i.allergene,
                   i.attivo,
                   i.creato_il,
                   i.aggiornato_il
            FROM ingredienti i
            """;

    @Override
    public List<Ingrediente> findByProdottoId(int idProdotto) throws SQLException {
        String sql = SELECT_INGREDIENTE_ASSOCIATO + """
                WHERE ip.id_prodotto = ?
                  AND i.attivo = TRUE
                ORDER BY i.nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Ingrediente> ingredienti = new ArrayList<>();
                while (resultSet.next()) {
                    ingredienti.add(mapRow(resultSet));
                }
                return ingredienti;
            }
        }
    }

    @Override
    public List<Ingrediente> findAllByProdottoIdForOwner(int idProdotto) throws SQLException {
        String sql = SELECT_INGREDIENTE_ASSOCIATO + """
                WHERE ip.id_prodotto = ?
                ORDER BY i.attivo DESC,
                         i.nome
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
    public List<Ingrediente> findAllActiveForOwner() throws SQLException {
        String sql = SELECT_INGREDIENTE_GLOBALE + """
                WHERE i.attivo = TRUE
                ORDER BY i.nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return mapRows(resultSet);
        }
    }

    @Override
    public Optional<Ingrediente> findByIdForOwner(long idIngrediente) throws SQLException {
        String sql = SELECT_INGREDIENTE_GLOBALE + """
                WHERE i.id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idIngrediente);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Ingrediente> findActiveByIdForOwner(long idIngrediente) throws SQLException {
        String sql = SELECT_INGREDIENTE_GLOBALE + """
                WHERE i.id = ?
                  AND i.attivo = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idIngrediente);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Ingrediente> findByProdottoIdAndIdForOwner(int idProdotto,
                                                               long idIngrediente) throws SQLException {
        String sql = SELECT_INGREDIENTE_ASSOCIATO + """
                WHERE ip.id_prodotto = ?
                  AND i.id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idIngrediente);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public boolean existsAssociationForOwner(int idProdotto, long idIngrediente) throws SQLException {
        String sql = """
                SELECT 1
                FROM ingredienti_prodotto
                WHERE id_prodotto = ?
                  AND id_ingrediente = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idIngrediente);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public Ingrediente createForOwner(String nome,
                                      String unitaMisura,
                                      boolean allergene,
                                      boolean attivo) throws SQLException {
        String sql = """
                INSERT INTO ingredienti (
                    nome,
                    unita_misura,
                    allergene,
                    attivo
                ) VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, nome);
            statement.setString(2, unitaMisura);
            statement.setBoolean(3, allergene);
            statement.setBoolean(4, attivo);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Nessun id generato per il nuovo ingrediente");
                }

                return findByIdForOwner(generatedKeys.getLong(1))
                        .orElseThrow(() -> new SQLException("Ingrediente creato non recuperabile"));
            }
        }
    }

    @Override
    public Ingrediente associateForOwner(int idProdotto,
                                         long idIngrediente,
                                         BigDecimal quantita) throws SQLException {
        String sql = """
                INSERT INTO ingredienti_prodotto (
                    id_prodotto,
                    id_ingrediente,
                    quantita
                ) VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idIngrediente);
            statement.setBigDecimal(3, quantita);

            statement.executeUpdate();

            return findByProdottoIdAndIdForOwner(idProdotto, idIngrediente)
                    .orElseThrow(() -> new SQLException("Ingrediente associato non recuperabile"));
        }
    }

    @Override
    public Optional<Ingrediente> updateForOwner(int idProdotto,
                                                long idIngrediente,
                                                String nome,
                                                String unitaMisura,
                                                BigDecimal quantita,
                                                boolean allergene,
                                                boolean attivo) throws SQLException {
        String updateIngredienteSql = """
                UPDATE ingredienti
                SET nome = ?,
                    unita_misura = ?,
                    allergene = ?,
                    attivo = ?
                WHERE id = ?
                """;
        String updateAssociazioneSql = """
                UPDATE ingredienti_prodotto
                SET quantita = ?
                WHERE id_prodotto = ?
                  AND id_ingrediente = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ingredienteStatement = connection.prepareStatement(updateIngredienteSql);
                 PreparedStatement associazioneStatement = connection.prepareStatement(updateAssociazioneSql)) {

                ingredienteStatement.setString(1, nome);
                ingredienteStatement.setString(2, unitaMisura);
                ingredienteStatement.setBoolean(3, allergene);
                ingredienteStatement.setBoolean(4, attivo);
                ingredienteStatement.setLong(5, idIngrediente);

                int ingredientiAggiornati = ingredienteStatement.executeUpdate();
                if (ingredientiAggiornati == 0) {
                    connection.rollback();
                    return Optional.empty();
                }

                associazioneStatement.setBigDecimal(1, quantita);
                associazioneStatement.setInt(2, idProdotto);
                associazioneStatement.setLong(3, idIngrediente);

                int associazioniAggiornate = associazioneStatement.executeUpdate();
                if (associazioniAggiornate == 0) {
                    connection.rollback();
                    return Optional.empty();
                }

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }

        return findByProdottoIdAndIdForOwner(idProdotto, idIngrediente);
    }

    @Override
    public boolean removeFromProdottoForOwner(int idProdotto, long idIngrediente) throws SQLException {
        String sql = """
                DELETE FROM ingredienti_prodotto
                WHERE id_prodotto = ?
                  AND id_ingrediente = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idIngrediente);

            return statement.executeUpdate() > 0;
        }
    }

    private List<Ingrediente> mapRows(ResultSet resultSet) throws SQLException {
        List<Ingrediente> ingredienti = new ArrayList<>();
        while (resultSet.next()) {
            ingredienti.add(mapRow(resultSet));
        }
        return ingredienti;
    }

    private Ingrediente mapRow(ResultSet resultSet) throws SQLException {
        return new Ingrediente(
                resultSet.getLong("id"),
                resultSet.getLong("id_prodotto"),
                resultSet.getString("nome"),
                resultSet.getString("unita_misura"),
                resultSet.getBigDecimal("quantita"),
                resultSet.getBoolean("allergene"),
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
