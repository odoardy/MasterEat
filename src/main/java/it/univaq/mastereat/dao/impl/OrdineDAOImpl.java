package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.OrdineDAO;
import it.univaq.mastereat.model.Caratteristica;
import it.univaq.mastereat.model.CaratteristicaRigaOrdine;
import it.univaq.mastereat.model.Ordine;
import it.univaq.mastereat.model.Prodotto;
import it.univaq.mastereat.model.RigaOrdine;
import it.univaq.mastereat.model.RigaOrdineDaCreare;
import it.univaq.mastereat.model.StatoOrdine;
import it.univaq.mastereat.model.StoricoStatoOrdine;
import it.univaq.mastereat.model.Utente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrdineDAOImpl implements OrdineDAO {

    private static final String SELECT_ORDINE = """
            SELECT id,
                   id_cliente,
                   stato,
                   creato_il,
                   confermato_il,
                   orario_consegna_richiesto,
                   minuti_consegna_stimati,
                   distanza_consegna_km,
                   prezzo_totale,
                   indirizzo_consegna_snapshot,
                   citta_consegna_snapshot,
                   cap_consegna_snapshot,
                   telefono_consegna_snapshot,
                   annullato_il,
                   motivo_annullamento
            FROM ordini
            """;

    private static final String SELECT_RIGHE = """
            SELECT r.id AS r_id,
                   r.id_ordine AS r_id_ordine,
                   r.id_prodotto AS r_id_prodotto,
                   r.nome_prodotto_snapshot AS r_nome_prodotto_snapshot,
                   r.prezzo_base_snapshot AS r_prezzo_base_snapshot,
                   r.minuti_preparazione_snapshot AS r_minuti_preparazione_snapshot,
                   r.quantita AS r_quantita,
                   r.totale_riga AS r_totale_riga,
                   r.creata_il AS r_creata_il,
                   cr.id AS cr_id,
                   cr.id_riga_ordine AS cr_id_riga_ordine,
                   cr.id_caratteristica AS cr_id_caratteristica,
                   cr.id_gruppo_caratteristiche_snapshot AS cr_id_gruppo_caratteristiche_snapshot,
                   cr.nome_caratteristica_snapshot AS cr_nome_caratteristica_snapshot,
                   cr.differenza_prezzo_snapshot AS cr_differenza_prezzo_snapshot
            FROM righe_ordine r
            LEFT JOIN caratteristiche_riga_ordine cr
              ON cr.id_riga_ordine = r.id
            """;

    @Override
    public Ordine create(Utente cliente, StatoOrdine stato) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                long idOrdine = insertOrdine(connection, cliente, stato);
                insertStoricoStato(connection, idOrdine, null, stato, cliente.getId(), "Creazione ordine");

                Ordine ordine = findById(connection, idOrdine)
                        .orElseThrow(() -> new SQLException("Ordine creato non recuperabile"));
                connection.commit();
                return ordine;
            } catch (SQLException exception) {
                rollbackQuietly(connection);
                throw exception;
            }
        }
    }

    @Override
    public Ordine createConfermato(Utente cliente,
                                   List<RigaOrdineDaCreare> righe,
                                   long idUtenteModifica) throws SQLException {
        return createConfermato(cliente, righe, idUtenteModifica, null);
    }

    @Override
    public Ordine createConfermato(Utente cliente,
                                   List<RigaOrdineDaCreare> righe,
                                   long idUtenteModifica,
                                   LocalDateTime orarioConsegnaRichiesto) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                long idOrdine = insertOrdine(connection, cliente, StatoOrdine.BOZZA, orarioConsegnaRichiesto);
                insertStoricoStato(connection, idOrdine, null, StatoOrdine.BOZZA, idUtenteModifica, "Creazione ordine web");

                for (RigaOrdineDaCreare riga : righe) {
                    insertRiga(
                            connection,
                            idOrdine,
                            riga.getProdotto(),
                            riga.getQuantita(),
                            riga.getCaratteristiche()
                    );
                }

                aggiornaRiepilogoOrdine(connection, idOrdine);
                confermaOrdine(connection, idOrdine, idUtenteModifica);

                Ordine ordine = findById(connection, idOrdine)
                        .orElseThrow(() -> new SQLException("Ordine confermato non recuperabile"));
                connection.commit();
                return ordine;
            } catch (SQLException exception) {
                rollbackQuietly(connection);
                throw exception;
            }
        }
    }

    @Override
    public Optional<Ordine> findById(long idOrdine) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            return findById(connection, idOrdine);
        }
    }

    @Override
    public RigaOrdine addRiga(long idOrdine,
                              Prodotto prodotto,
                              int quantita,
                              List<Caratteristica> caratteristiche) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                long idRigaOrdine = insertRiga(connection, idOrdine, prodotto, quantita, caratteristiche);
                aggiornaRiepilogoOrdine(connection, idOrdine);

                RigaOrdine rigaOrdine = findRigaById(connection, idRigaOrdine)
                        .orElseThrow(() -> new SQLException("Riga ordine creata non recuperabile"));
                connection.commit();
                return rigaOrdine;
            } catch (SQLException exception) {
                rollbackQuietly(connection);
                throw exception;
            }
        }
    }

    @Override
    public List<RigaOrdine> findRigheByOrdineId(long idOrdine) throws SQLException {
        String sql = SELECT_RIGHE + """
                WHERE r.id_ordine = ?
                ORDER BY r.id,
                         cr.id
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idOrdine);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRighe(resultSet);
            }
        }
    }

    @Override
    public Map<Long, Integer> countProdottiByOrdineIds(List<Long> idOrdini) throws SQLException {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        if (idOrdini == null || idOrdini.isEmpty()) {
            return counts;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT id_ordine,
                       COALESCE(SUM(quantita), 0) AS numero_prodotti
                FROM righe_ordine
                WHERE id_ordine IN (
                """);
        for (int index = 0; index < idOrdini.size(); index++) {
            if (index > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append("""
                )
                GROUP BY id_ordine
                """);

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int index = 0; index < idOrdini.size(); index++) {
                statement.setLong(index + 1, idOrdini.get(index));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    counts.put(resultSet.getLong("id_ordine"), resultSet.getInt("numero_prodotti"));
                }
            }
        }

        return counts;
    }

    @Override
    public BigDecimal calculateTotale(long idOrdine) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            return calculateTotale(connection, idOrdine);
        }
    }

    @Override
    public int calculateTempoPreparazione(long idOrdine) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            return calculateTempoPreparazione(connection, idOrdine);
        }
    }

    @Override
    public Optional<Ordine> conferma(long idOrdine,
                                     long idUtenteModifica,
                                     StatoOrdine statoPrecedente) throws SQLException {
        String sql = """
                UPDATE ordini
                SET stato = ?,
                    confermato_il = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND stato = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, StatoOrdine.INSERITO.name());
                statement.setLong(2, idOrdine);
                statement.setString(3, statoPrecedente.name());

                int updatedRows = statement.executeUpdate();
                if (updatedRows == 0) {
                    rollbackQuietly(connection);
                    return Optional.empty();
                }

                insertStoricoStato(
                        connection,
                        idOrdine,
                        statoPrecedente,
                        StatoOrdine.INSERITO,
                        idUtenteModifica,
                        "Conferma ordine"
                );

                Ordine ordine = findById(connection, idOrdine)
                        .orElseThrow(() -> new SQLException("Ordine confermato non recuperabile"));
                connection.commit();
                return Optional.of(ordine);
            } catch (SQLException exception) {
                rollbackQuietly(connection);
                throw exception;
            }
        }
    }

    @Override
    public Optional<Ordine> aggiornaStato(long idOrdine,
                                          long idUtenteModifica,
                                          StatoOrdine statoPrecedente,
                                          StatoOrdine statoNuovo) throws SQLException {
        String sql = """
                UPDATE ordini
                SET stato = ?
                WHERE id = ?
                  AND stato = ?
                  AND stato NOT IN ('ANNULLATO', 'CONSEGNATO')
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, statoNuovo.name());
                statement.setLong(2, idOrdine);
                statement.setString(3, statoPrecedente.name());

                int updatedRows = statement.executeUpdate();
                if (updatedRows == 0) {
                    rollbackQuietly(connection);
                    return Optional.empty();
                }

                insertStoricoStato(
                        connection,
                        idOrdine,
                        statoPrecedente,
                        statoNuovo,
                        idUtenteModifica,
                        "Cambio stato ordine"
                );

                Ordine ordine = findById(connection, idOrdine)
                        .orElseThrow(() -> new SQLException("Ordine aggiornato non recuperabile"));
                connection.commit();
                return Optional.of(ordine);
            } catch (SQLException exception) {
                rollbackQuietly(connection);
                throw exception;
            }
        }
    }

    @Override
    public List<Ordine> findByFilters(StatoOrdine stato,
                                      LocalDate dataDa,
                                      LocalDate dataA) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_ORDINE);
        sql.append("WHERE 1 = 1");

        List<Object> parameters = new ArrayList<>();
        if (stato == null) {
            sql.append(" AND stato <> ?");
            parameters.add(StatoOrdine.BOZZA.name());
        } else {
            sql.append(" AND stato = ?");
            parameters.add(stato.name());
        }
        if (dataDa != null) {
            sql.append(" AND creato_il >= ?");
            parameters.add(Timestamp.valueOf(dataDa.atStartOfDay()));
        }
        if (dataA != null) {
            sql.append(" AND creato_il < ?");
            parameters.add(Timestamp.valueOf(dataA.plusDays(1).atStartOfDay()));
        }

        sql.append(" ORDER BY creato_il DESC, id DESC");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrdini(resultSet);
            }
        }
    }

    @Override
    public List<Ordine> findAllByFilters(StatoOrdine stato,
                                         LocalDate dataDa,
                                         LocalDate dataA) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_ORDINE);
        sql.append("WHERE 1 = 1");

        List<Object> parameters = new ArrayList<>();
        if (stato != null) {
            sql.append(" AND stato = ?");
            parameters.add(stato.name());
        } else {
            sql.append(" AND stato <> ?");
            parameters.add(StatoOrdine.BOZZA.name());
        }
        if (dataDa != null) {
            sql.append(" AND creato_il >= ?");
            parameters.add(Timestamp.valueOf(dataDa.atStartOfDay()));
        }
        if (dataA != null) {
            sql.append(" AND creato_il < ?");
            parameters.add(Timestamp.valueOf(dataA.plusDays(1).atStartOfDay()));
        }

        sql.append(" ORDER BY creato_il DESC, id DESC");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrdini(resultSet);
            }
        }
    }

    @Override
    public List<Ordine> findByClienteId(long idCliente) throws SQLException {
        return findByClienteIdAndFilters(idCliente, null, null, null);
    }

    @Override
    public List<Ordine> findByClienteIdAndFilters(long idCliente,
                                                  StatoOrdine stato,
                                                  LocalDate dataDa,
                                                  LocalDate dataA) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_ORDINE);
        sql.append("WHERE id_cliente = ?");

        List<Object> parameters = new ArrayList<>();
        parameters.add(idCliente);
        if (stato != null) {
            sql.append(" AND stato = ?");
            parameters.add(stato.name());
        }
        if (dataDa != null) {
            sql.append(" AND creato_il >= ?");
            parameters.add(Timestamp.valueOf(dataDa.atStartOfDay()));
        }
        if (dataA != null) {
            sql.append(" AND creato_il < ?");
            parameters.add(Timestamp.valueOf(dataA.plusDays(1).atStartOfDay()));
        }

        sql.append(" ORDER BY creato_il DESC, id DESC");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrdini(resultSet);
            }
        }
    }

    @Override
    public List<StoricoStatoOrdine> findStoricoByOrdineId(long idOrdine) throws SQLException {
        String sql = """
                SELECT s.id,
                       s.id_ordine,
                       s.stato_precedente,
                       s.stato_nuovo,
                       s.id_utente_modifica,
                       u.username,
                       u.nome,
                       u.cognome,
                       u.ruolo,
                       s.modificato_il,
                       s.nota
                FROM storico_stati_ordine s
                LEFT JOIN utenti u
                  ON s.id_utente_modifica = u.id
                WHERE s.id_ordine = ?
                ORDER BY s.modificato_il,
                         s.id
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idOrdine);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<StoricoStatoOrdine> storico = new ArrayList<>();
                while (resultSet.next()) {
                    storico.add(mapStoricoStatoOrdine(resultSet));
                }
                return storico;
            }
        }
    }

    @Override
    public List<StoricoStatoOrdine> findStoricoOperatoriByOrdineId(long idOrdine) throws SQLException {
        String sql = """
                SELECT s.id,
                       s.id_ordine,
                       s.stato_precedente,
                       s.stato_nuovo,
                       s.id_utente_modifica,
                       u.username,
                       u.nome,
                       u.cognome,
                       u.ruolo,
                       s.modificato_il,
                       s.nota
                FROM storico_stati_ordine s
                JOIN utenti u
                  ON s.id_utente_modifica = u.id
                WHERE s.id_ordine = ?
                  AND u.ruolo IN ('PERSONALE', 'PROPRIETARIO')
                ORDER BY s.modificato_il,
                         s.id
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idOrdine);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<StoricoStatoOrdine> storico = new ArrayList<>();
                while (resultSet.next()) {
                    storico.add(mapStoricoStatoOrdine(resultSet));
                }
                return storico;
            }
        }
    }

    @Override
    public Optional<Ordine> annulla(long idOrdine,
                                    long idUtenteModifica,
                                    StatoOrdine statoPrecedente,
                                    String motivo) throws SQLException {
        String sql = """
                UPDATE ordini
                SET stato = ?,
                    annullato_il = CURRENT_TIMESTAMP,
                    motivo_annullamento = ?
                WHERE id = ?
                  AND stato IN ('BOZZA', 'INSERITO')
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, StatoOrdine.ANNULLATO.name());
                statement.setString(2, motivo);
                statement.setLong(3, idOrdine);

                int updatedRows = statement.executeUpdate();
                if (updatedRows == 0) {
                    rollbackQuietly(connection);
                    return Optional.empty();
                }

                insertStoricoStato(
                        connection,
                        idOrdine,
                        statoPrecedente,
                        StatoOrdine.ANNULLATO,
                        idUtenteModifica,
                        motivo
                );

                Ordine ordine = findById(connection, idOrdine)
                        .orElseThrow(() -> new SQLException("Ordine annullato non recuperabile"));
                connection.commit();
                return Optional.of(ordine);
            } catch (SQLException exception) {
                rollbackQuietly(connection);
                throw exception;
            }
        }
    }

    private Optional<Ordine> findById(Connection connection, long idOrdine) throws SQLException {
        String sql = SELECT_ORDINE + """
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idOrdine);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapOrdine(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    private Optional<RigaOrdine> findRigaById(Connection connection, long idRigaOrdine) throws SQLException {
        String sql = SELECT_RIGHE + """
                WHERE r.id = ?
                ORDER BY cr.id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idRigaOrdine);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<RigaOrdine> righe = mapRighe(resultSet);
                if (righe.isEmpty()) {
                    return Optional.empty();
                }

                return Optional.of(righe.get(0));
            }
        }
    }

    private long insertOrdine(Connection connection, Utente cliente, StatoOrdine stato) throws SQLException {
        return insertOrdine(connection, cliente, stato, null);
    }

    private long insertOrdine(Connection connection,
                              Utente cliente,
                              StatoOrdine stato,
                              LocalDateTime orarioConsegnaRichiesto) throws SQLException {
        String sql = """
                INSERT INTO ordini (
                    id_cliente,
                    stato,
                    orario_consegna_richiesto,
                    indirizzo_consegna_snapshot,
                    citta_consegna_snapshot,
                    cap_consegna_snapshot,
                    telefono_consegna_snapshot
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, cliente.getId());
            statement.setString(2, stato.name());
            if (orarioConsegnaRichiesto != null) {
                statement.setTimestamp(3, Timestamp.valueOf(orarioConsegnaRichiesto));
            } else {
                statement.setNull(3, Types.TIMESTAMP);
            }
            statement.setString(4, cliente.getIndirizzo());
            statement.setString(5, cliente.getCitta());
            statement.setString(6, cliente.getCap());
            statement.setString(7, cliente.getTelefono());
            statement.executeUpdate();

            return getGeneratedId(statement);
        }
    }

    private long insertRiga(Connection connection,
                            long idOrdine,
                            Prodotto prodotto,
                            int quantita,
                            List<Caratteristica> caratteristiche) throws SQLException {
        String sql = """
                INSERT INTO righe_ordine (
                    id_ordine,
                    id_prodotto,
                    nome_prodotto_snapshot,
                    prezzo_base_snapshot,
                    minuti_preparazione_snapshot,
                    quantita,
                    totale_riga
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        BigDecimal totaleRiga = calculateTotaleRiga(prodotto, quantita, caratteristiche);

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, idOrdine);
            statement.setLong(2, prodotto.getId());
            statement.setString(3, prodotto.getNome());
            statement.setBigDecimal(4, prodotto.getPrezzoBase());
            statement.setInt(5, prodotto.getMinutiPreparazione());
            statement.setInt(6, quantita);
            statement.setBigDecimal(7, totaleRiga);
            statement.executeUpdate();

            long idRigaOrdine = getGeneratedId(statement);
            insertCaratteristicheRiga(connection, idRigaOrdine, caratteristiche);
            return idRigaOrdine;
        }
    }

    private void confermaOrdine(Connection connection, long idOrdine, long idUtenteModifica) throws SQLException {
        String sql = """
                UPDATE ordini
                SET stato = ?,
                    confermato_il = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND stato = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, StatoOrdine.INSERITO.name());
            statement.setLong(2, idOrdine);
            statement.setString(3, StatoOrdine.BOZZA.name());

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Ordine non confermabile");
            }
        }

        insertStoricoStato(
                connection,
                idOrdine,
                StatoOrdine.BOZZA,
                StatoOrdine.INSERITO,
                idUtenteModifica,
                "Conferma ordine web"
        );
    }

    private void insertCaratteristicheRiga(Connection connection,
                                          long idRigaOrdine,
                                          List<Caratteristica> caratteristiche) throws SQLException {
        if (caratteristiche == null || caratteristiche.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO caratteristiche_riga_ordine (
                    id_riga_ordine,
                    id_caratteristica,
                    id_gruppo_caratteristiche_snapshot,
                    nome_caratteristica_snapshot,
                    differenza_prezzo_snapshot
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Caratteristica caratteristica : caratteristiche) {
                statement.setLong(1, idRigaOrdine);
                statement.setLong(2, caratteristica.getId());
                setNullableLong(statement, 3, caratteristica.getIdGruppoCaratteristiche());
                statement.setString(4, caratteristica.getNome());
                statement.setBigDecimal(5, caratteristica.getDifferenzaPrezzo());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void insertStoricoStato(Connection connection,
                                    long idOrdine,
                                    StatoOrdine statoPrecedente,
                                    StatoOrdine statoNuovo,
                                    long idUtenteModifica,
                                    String nota) throws SQLException {
        String sql = """
                INSERT INTO storico_stati_ordine (
                    id_ordine,
                    stato_precedente,
                    stato_nuovo,
                    id_utente_modifica,
                    nota
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idOrdine);
            if (statoPrecedente == null) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, statoPrecedente.name());
            }
            statement.setString(3, statoNuovo.name());
            statement.setLong(4, idUtenteModifica);
            statement.setString(5, nota);
            statement.executeUpdate();
        }
    }

    private void aggiornaRiepilogoOrdine(Connection connection, long idOrdine) throws SQLException {
        BigDecimal totale = calculateTotale(connection, idOrdine);
        int minutiPreparazione = calculateTempoPreparazione(connection, idOrdine);

        String sql = """
                UPDATE ordini
                SET prezzo_totale = ?,
                    minuti_consegna_stimati = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, totale);
            if (minutiPreparazione > 0) {
                statement.setInt(2, minutiPreparazione);
            } else {
                statement.setNull(2, Types.INTEGER);
            }
            statement.setLong(3, idOrdine);
            statement.executeUpdate();
        }
    }

    private BigDecimal calculateTotale(Connection connection, long idOrdine) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(totale_riga), 0) AS totale
                FROM righe_ordine
                WHERE id_ordine = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idOrdine);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("totale");
                }

                return BigDecimal.ZERO;
            }
        }
    }

    private int calculateTempoPreparazione(Connection connection, long idOrdine) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(minuti_preparazione_snapshot * quantita), 0) AS minuti
                FROM righe_ordine
                WHERE id_ordine = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idOrdine);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("minuti");
                }

                return 0;
            }
        }
    }

    private BigDecimal calculateTotaleRiga(Prodotto prodotto,
                                           int quantita,
                                           List<Caratteristica> caratteristiche) {
        BigDecimal prezzoUnitario = prodotto.getPrezzoBase();
        if (caratteristiche != null) {
            for (Caratteristica caratteristica : caratteristiche) {
                prezzoUnitario = prezzoUnitario.add(caratteristica.getDifferenzaPrezzo());
            }
        }

        return prezzoUnitario.multiply(BigDecimal.valueOf(quantita));
    }

    private List<RigaOrdine> mapRighe(ResultSet resultSet) throws SQLException {
        Map<Long, RigaOrdine> righeById = new LinkedHashMap<>();

        while (resultSet.next()) {
            long idRiga = resultSet.getLong("r_id");
            RigaOrdine riga = righeById.get(idRiga);

            if (riga == null) {
                riga = new RigaOrdine(
                        idRiga,
                        resultSet.getLong("r_id_ordine"),
                        resultSet.getLong("r_id_prodotto"),
                        resultSet.getString("r_nome_prodotto_snapshot"),
                        resultSet.getBigDecimal("r_prezzo_base_snapshot"),
                        resultSet.getInt("r_minuti_preparazione_snapshot"),
                        resultSet.getInt("r_quantita"),
                        resultSet.getBigDecimal("r_totale_riga"),
                        getTimestampAsString(resultSet, "r_creata_il"),
                        new ArrayList<>()
                );
                righeById.put(idRiga, riga);
            }

            long idCaratteristicaRiga = resultSet.getLong("cr_id");
            if (!resultSet.wasNull()) {
                long idGruppoCaratteristiche = resultSet.getLong("cr_id_gruppo_caratteristiche_snapshot");
                Long idGruppoCaratteristicheNullable = resultSet.wasNull() ? null : idGruppoCaratteristiche;

                riga.getCaratteristiche().add(new CaratteristicaRigaOrdine(
                        idCaratteristicaRiga,
                        resultSet.getLong("cr_id_riga_ordine"),
                        resultSet.getLong("cr_id_caratteristica"),
                        idGruppoCaratteristicheNullable,
                        resultSet.getString("cr_nome_caratteristica_snapshot"),
                        resultSet.getBigDecimal("cr_differenza_prezzo_snapshot")
                ));
            }
        }

        return new ArrayList<>(righeById.values());
    }

    private Ordine mapOrdine(ResultSet resultSet) throws SQLException {
        int minutiConsegnaStimati = resultSet.getInt("minuti_consegna_stimati");
        Integer minutiConsegnaStimatiNullable = resultSet.wasNull() ? null : minutiConsegnaStimati;

        return new Ordine(
                resultSet.getLong("id"),
                resultSet.getLong("id_cliente"),
                StatoOrdine.valueOf(resultSet.getString("stato")),
                getTimestampAsString(resultSet, "creato_il"),
                getTimestampAsString(resultSet, "confermato_il"),
                getTimestampAsString(resultSet, "orario_consegna_richiesto"),
                minutiConsegnaStimatiNullable,
                resultSet.getBigDecimal("distanza_consegna_km"),
                resultSet.getBigDecimal("prezzo_totale"),
                resultSet.getString("indirizzo_consegna_snapshot"),
                resultSet.getString("citta_consegna_snapshot"),
                resultSet.getString("cap_consegna_snapshot"),
                resultSet.getString("telefono_consegna_snapshot"),
                getTimestampAsString(resultSet, "annullato_il"),
                resultSet.getString("motivo_annullamento")
        );
    }

    private List<Ordine> mapOrdini(ResultSet resultSet) throws SQLException {
        List<Ordine> ordini = new ArrayList<>();
        while (resultSet.next()) {
            ordini.add(mapOrdine(resultSet));
        }
        return ordini;
    }

    private StoricoStatoOrdine mapStoricoStatoOrdine(ResultSet resultSet) throws SQLException {
        String statoPrecedente = resultSet.getString("stato_precedente");
        StatoOrdine statoPrecedenteNullable = statoPrecedente != null
                ? StatoOrdine.valueOf(statoPrecedente)
                : null;

        long idUtenteModifica = resultSet.getLong("id_utente_modifica");
        Long idUtenteModificaNullable = resultSet.wasNull() ? null : idUtenteModifica;

        return new StoricoStatoOrdine(
                resultSet.getLong("id"),
                resultSet.getLong("id_ordine"),
                statoPrecedenteNullable,
                StatoOrdine.valueOf(resultSet.getString("stato_nuovo")),
                idUtenteModificaNullable,
                resultSet.getString("username"),
                resultSet.getString("nome"),
                resultSet.getString("cognome"),
                resultSet.getString("ruolo"),
                getTimestampAsString(resultSet, "modificato_il"),
                resultSet.getString("nota")
        );
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.get(index);
            int parameterIndex = index + 1;

            if (parameter instanceof Timestamp timestamp) {
                statement.setTimestamp(parameterIndex, timestamp);
            } else if (parameter instanceof Long longValue) {
                statement.setLong(parameterIndex, longValue);
            } else if (parameter instanceof Integer integerValue) {
                statement.setInt(parameterIndex, integerValue);
            } else {
                statement.setString(parameterIndex, parameter.toString());
            }
        }
    }

    private long getGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
        }

        throw new SQLException("Nessun id generato");
    }

    private String getTimestampAsString(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime().toString() : null;
    }

    private void setNullableLong(PreparedStatement statement, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.BIGINT);
        } else {
            statement.setLong(parameterIndex, value);
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // best effort rollback
        }
    }
}
