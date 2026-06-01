package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.CaratteristicaDAO;
import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.model.Caratteristica;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CaratteristicaDAOImpl implements CaratteristicaDAO {

    private static final String SELECT_CARATTERISTICA = """
            SELECT c.id,
                   c.id_prodotto,
                   c.id_gruppo_caratteristiche,
                   g.nome AS nome_gruppo_caratteristiche,
                   g.descrizione AS descrizione_gruppo_caratteristiche,
                   c.nome,
                   c.descrizione,
                   c.differenza_prezzo,
                   c.selezionata_default,
                   c.attiva,
                   c.creato_il,
                   c.aggiornato_il
            FROM caratteristiche c
            LEFT JOIN gruppi_caratteristiche g
              ON g.id = c.id_gruppo_caratteristiche
             AND g.id_prodotto = c.id_prodotto
            """;

    @Override
    public List<Caratteristica> findByProdottoId(int idProdotto) throws SQLException {
        Map<Long, List<Caratteristica>> caratteristicheByProdottoId = findByProdottoIds(List.of((long) idProdotto));
        return caratteristicheByProdottoId.getOrDefault((long) idProdotto, Collections.emptyList());
    }

    @Override
    public List<Caratteristica> findAllByProdottoIdForOwner(int idProdotto) throws SQLException {
        String sql = SELECT_CARATTERISTICA + """
                WHERE c.id_prodotto = ?
                ORDER BY c.attiva DESC,
                         c.id_gruppo_caratteristiche,
                         c.nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Caratteristica> caratteristiche = new ArrayList<>();
                while (resultSet.next()) {
                    caratteristiche.add(mapRow(resultSet));
                }
                return caratteristiche;
            }
        }
    }

    @Override
    public Caratteristica createForOwner(int idProdotto,
                                         Long idGruppoCaratteristiche,
                                         String nome,
                                         String descrizione,
                                         BigDecimal differenzaPrezzo,
                                         boolean selezionataDefault,
                                         boolean attiva) throws SQLException {
        String sql = """
                INSERT INTO caratteristiche (
                    id_prodotto,
                    id_gruppo_caratteristiche,
                    nome,
                    descrizione,
                    differenza_prezzo,
                    selezionata_default,
                    attiva
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, idProdotto);
            setNullableLong(statement, 2, idGruppoCaratteristiche);
            statement.setString(3, nome);
            statement.setString(4, descrizione);
            statement.setBigDecimal(5, differenzaPrezzo);
            statement.setBoolean(6, selezionataDefault);
            statement.setBoolean(7, attiva);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Nessun id generato per la nuova caratteristica");
                }

                return findByProdottoIdAndIdForOwner(idProdotto, generatedKeys.getInt(1))
                        .orElseThrow(() -> new SQLException("Caratteristica creata non recuperabile"));
            }
        }
    }

    @Override
    public Map<Long, List<Caratteristica>> findByProdottoIds(List<Long> idProdotti) throws SQLException {
        if (idProdotti == null || idProdotti.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = idProdotti.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = SELECT_CARATTERISTICA + """
                WHERE c.id_prodotto IN (
                """ + placeholders + """
                )
                  AND c.attiva = TRUE
                ORDER BY c.id_prodotto,
                         c.id_gruppo_caratteristiche,
                         c.nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int index = 0; index < idProdotti.size(); index++) {
                statement.setLong(index + 1, idProdotti.get(index));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Long, List<Caratteristica>> caratteristicheByProdottoId = new LinkedHashMap<>();
                while (resultSet.next()) {
                    Caratteristica caratteristica = mapRow(resultSet);
                    caratteristicheByProdottoId
                            .computeIfAbsent(caratteristica.getIdProdotto(), id -> new ArrayList<>())
                            .add(caratteristica);
                }
                return caratteristicheByProdottoId;
            }
        }
    }

    @Override
    public Optional<Caratteristica> findByProdottoIdAndIdForOwner(int idProdotto,
                                                                  int idCaratteristica) throws SQLException {
        String sql = SELECT_CARATTERISTICA + """
                WHERE c.id_prodotto = ?
                  AND c.id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setInt(2, idCaratteristica);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Caratteristica> updateForOwner(int idProdotto,
                                                   int idCaratteristica,
                                                   Long idGruppoCaratteristiche,
                                                   String nome,
                                                   String descrizione,
                                                   BigDecimal differenzaPrezzo,
                                                   boolean selezionataDefault,
                                                   boolean attiva) throws SQLException {
        String sql = """
                UPDATE caratteristiche
                SET id_gruppo_caratteristiche = ?,
                    nome = ?,
                    descrizione = ?,
                    differenza_prezzo = ?,
                    selezionata_default = ?,
                    attiva = ?
                WHERE id_prodotto = ?
                  AND id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setNullableLong(statement, 1, idGruppoCaratteristiche);
            statement.setString(2, nome);
            statement.setString(3, descrizione);
            statement.setBigDecimal(4, differenzaPrezzo);
            statement.setBoolean(5, selezionataDefault);
            statement.setBoolean(6, attiva);
            statement.setInt(7, idProdotto);
            statement.setInt(8, idCaratteristica);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                return Optional.empty();
            }

            return findByProdottoIdAndIdForOwner(idProdotto, idCaratteristica);
        }
    }

    @Override
    public boolean deleteFromProdotto(int idProdotto, int idCaratteristica) throws SQLException {
        String sql = """
                UPDATE caratteristiche
                SET attiva = FALSE,
                    aggiornato_il = CURRENT_TIMESTAMP
                WHERE id_prodotto = ?
                  AND id = ?
                  AND attiva = TRUE
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProdotto);
            statement.setInt(2, idCaratteristica);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsOtherDefaultInGroupForOwner(int idProdotto,
                                                     long idGruppoCaratteristiche,
                                                     Long idCaratteristicaDaEscludere) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT 1
                FROM caratteristiche
                WHERE id_prodotto = ?
                  AND id_gruppo_caratteristiche = ?
                  AND selezionata_default = TRUE
                  AND attiva = TRUE
                """);
        if (idCaratteristicaDaEscludere != null) {
            sql.append("  AND id <> ?\n");
        }
        sql.append("LIMIT 1");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            statement.setInt(1, idProdotto);
            statement.setLong(2, idGruppoCaratteristiche);
            if (idCaratteristicaDaEscludere != null) {
                statement.setLong(3, idCaratteristicaDaEscludere);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Caratteristica mapRow(ResultSet resultSet) throws SQLException {
        long idGruppoCaratteristiche = resultSet.getLong("id_gruppo_caratteristiche");
        Long idGruppoCaratteristicheNullable = resultSet.wasNull() ? null : idGruppoCaratteristiche;

        return new Caratteristica(
                resultSet.getLong("id"),
                resultSet.getLong("id_prodotto"),
                idGruppoCaratteristicheNullable,
                resultSet.getString("nome_gruppo_caratteristiche"),
                resultSet.getString("descrizione_gruppo_caratteristiche"),
                resultSet.getString("nome"),
                resultSet.getString("descrizione"),
                resultSet.getBigDecimal("differenza_prezzo"),
                resultSet.getBoolean("selezionata_default"),
                resultSet.getBoolean("attiva"),
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
}
