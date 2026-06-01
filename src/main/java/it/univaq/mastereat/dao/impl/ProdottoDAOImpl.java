package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.ProdottoDAO;
import it.univaq.mastereat.model.Prodotto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProdottoDAOImpl implements ProdottoDAO {

    private static final String SELECT_PRODOTTO = """
            SELECT p.id,
                   p.id_categoria,
                   p.nome,
                   p.descrizione,
                   p.prezzo_base,
                   p.minuti_preparazione,
                   p.descrizione_preparazione,
                   p.attivo,
                   p.creato_il,
                   p.aggiornato_il
            FROM prodotti p
            LEFT JOIN categorie_prodotto c
              ON p.id_categoria = c.id
            """;

    private static final String WHERE_PRODOTTO_VISIBILE = """
            WHERE p.attivo = TRUE
              AND (p.id_categoria IS NULL OR c.attiva = TRUE)
            """;

    @Override
    public List<Prodotto> findAll() throws SQLException {
        String sql = SELECT_PRODOTTO + WHERE_PRODOTTO_VISIBILE + """
                ORDER BY p.nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return mapRows(resultSet);
        }
    }

    @Override
    public List<Prodotto> findAllForOwner() throws SQLException {
        String sql = SELECT_PRODOTTO + """
                ORDER BY p.attivo DESC,
                         p.nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return mapRows(resultSet);
        }
    }

    @Override
    public Prodotto createForOwner(Long idCategoria,
                                   String nome,
                                   String descrizione,
                                   BigDecimal prezzoBase,
                                   int minutiPreparazione,
                                   String descrizionePreparazione,
                                   boolean attivo) throws SQLException {
        String sql = """
                INSERT INTO prodotti (
                    id_categoria,
                    nome,
                    descrizione,
                    prezzo_base,
                    minuti_preparazione,
                    descrizione_preparazione,
                    attivo
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setNullableLong(statement, 1, idCategoria);
            statement.setString(2, nome);
            statement.setString(3, descrizione);
            statement.setBigDecimal(4, prezzoBase);
            statement.setInt(5, minutiPreparazione);
            statement.setString(6, descrizionePreparazione);
            statement.setBoolean(7, attivo);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Nessun id generato per il nuovo prodotto");
                }

                return findByIdForOwner(generatedKeys.getInt(1))
                        .orElseThrow(() -> new SQLException("Prodotto creato non recuperabile"));
            }
        }
    }

    @Override
    public Optional<Prodotto> findById(int id) throws SQLException {
        String sql = SELECT_PRODOTTO + WHERE_PRODOTTO_VISIBILE + """
                  AND p.id = ?
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
    public Optional<Prodotto> findByIdForOwner(int id) throws SQLException {
        String sql = SELECT_PRODOTTO + """
                WHERE p.id = ?
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
    public Optional<Prodotto> updateForOwner(int id,
                                             Long idCategoria,
                                             String nome,
                                             String descrizione,
                                             BigDecimal prezzoBase,
                                             int minutiPreparazione,
                                             String descrizionePreparazione,
                                             boolean attivo) throws SQLException {
        String sql = """
                UPDATE prodotti
                SET id_categoria = ?,
                    nome = ?,
                    descrizione = ?,
                    prezzo_base = ?,
                    minuti_preparazione = ?,
                    descrizione_preparazione = ?,
                    attivo = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setNullableLong(statement, 1, idCategoria);
            statement.setString(2, nome);
            statement.setString(3, descrizione);
            statement.setBigDecimal(4, prezzoBase);
            statement.setInt(5, minutiPreparazione);
            statement.setString(6, descrizionePreparazione);
            statement.setBoolean(7, attivo);
            statement.setInt(8, id);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                return Optional.empty();
            }

            return findByIdForOwner(id);
        }
    }

    @Override
    public List<Prodotto> search(String nome, BigDecimal prezzoMin, BigDecimal prezzoMax) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_PRODOTTO);
        sql.append("WHERE p.attivo = TRUE AND (p.id_categoria IS NULL OR c.attiva = TRUE)");

        List<Object> parameters = new ArrayList<>();
        String nomeNormalizzato = normalize(nome);
        if (nomeNormalizzato != null) {
            sql.append(" AND LOWER(p.nome) LIKE ?");
            parameters.add("%" + nomeNormalizzato.toLowerCase(Locale.ROOT) + "%");
        }
        if (prezzoMin != null) {
            sql.append(" AND p.prezzo_base >= ?");
            parameters.add(prezzoMin);
        }
        if (prezzoMax != null) {
            sql.append(" AND p.prezzo_base <= ?");
            parameters.add(prezzoMax);
        }

        sql.append(" ORDER BY p.nome");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int index = 0; index < parameters.size(); index++) {
                Object parameter = parameters.get(index);
                if (parameter instanceof BigDecimal value) {
                    statement.setBigDecimal(index + 1, value);
                } else {
                    statement.setString(index + 1, parameter.toString());
                }
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        }
    }

    private List<Prodotto> mapRows(ResultSet resultSet) throws SQLException {
        List<Prodotto> prodotti = new ArrayList<>();
        while (resultSet.next()) {
            prodotti.add(mapRow(resultSet));
        }
        return prodotti;
    }

    private Prodotto mapRow(ResultSet resultSet) throws SQLException {
        long idCategoria = resultSet.getLong("id_categoria");
        Long idCategoriaNullable = resultSet.wasNull() ? null : idCategoria;

        return new Prodotto(
                resultSet.getLong("id"),
                idCategoriaNullable,
                resultSet.getString("nome"),
                resultSet.getString("descrizione"),
                resultSet.getBigDecimal("prezzo_base"),
                resultSet.getInt("minuti_preparazione"),
                resultSet.getString("descrizione_preparazione"),
                resultSet.getBoolean("attivo"),
                getTimestampAsString(resultSet, "creato_il"),
                getTimestampAsString(resultSet, "aggiornato_il")
        );
    }

    private String getTimestampAsString(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime().toString() : null;
    }

    private void setNullableLong(PreparedStatement statement, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.BIGINT);
            return;
        }

        statement.setLong(parameterIndex, value);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
