package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.dao.StatisticheDAO;
import it.univaq.mastereat.dto.web.owner.OwnerStatisticheResponse.ProdottoStatisticaResponse;
import it.univaq.mastereat.dto.web.owner.OwnerStatisticheResponse.RiepilogoStatisticheResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatisticheDAOImpl implements StatisticheDAO {

    private static final String PRODOTTI_PERIODO_BASE = """
            SELECT r.id_prodotto,
                   COALESCE(p.nome, MIN(r.nome_prodotto_snapshot)) AS nome_prodotto,
                   COALESCE(SUM(r.quantita), 0) AS quantita_ordinata,
                   COALESCE(SUM(r.totale_riga), 0) AS ricavo_generato
            FROM righe_ordine r
            JOIN ordini o
              ON o.id = r.id_ordine
            LEFT JOIN prodotti p
              ON p.id = r.id_prodotto
            WHERE o.stato NOT IN ('BOZZA', 'ANNULLATO')
              AND COALESCE(o.confermato_il, o.creato_il) >= ?
              AND COALESCE(o.confermato_il, o.creato_il) < ?
            GROUP BY r.id_prodotto,
                     p.nome
            """;

    @Override
    public RiepilogoStatisticheResponse getRiepilogoOrdini(LocalDate dataDaInclusa,
                                                           LocalDate dataAEsclusa) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(prezzo_totale), 0) AS incasso_totale,
                       COUNT(*) AS numero_ordini
                FROM ordini
                WHERE stato NOT IN ('BOZZA', 'ANNULLATO')
                  AND COALESCE(confermato_il, creato_il) >= ?
                  AND COALESCE(confermato_il, creato_il) < ?
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setPeriodParameters(statement, dataDaInclusa, dataAEsclusa);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new RiepilogoStatisticheResponse(
                            resultSet.getBigDecimal("incasso_totale"),
                            resultSet.getInt("numero_ordini")
                    );
                }

                return new RiepilogoStatisticheResponse();
            }
        }
    }

    @Override
    public List<ProdottoStatisticaResponse> findProdottiPiuOrdinati(LocalDate dataDaInclusa,
                                                                    LocalDate dataAEsclusa,
                                                                    int limit) throws SQLException {
        String sql = PRODOTTI_PERIODO_BASE + """
                ORDER BY quantita_ordinata DESC,
                         ricavo_generato DESC,
                         nome_prodotto
                LIMIT ?
                """;

        return findProdottiPeriodo(dataDaInclusa, dataAEsclusa, limit, sql);
    }

    @Override
    public List<ProdottoStatisticaResponse> findProdottiMenoOrdinati(LocalDate dataDaInclusa,
                                                                     LocalDate dataAEsclusa,
                                                                     int limit) throws SQLException {
        String sql = PRODOTTI_PERIODO_BASE + """
                ORDER BY quantita_ordinata ASC,
                         ricavo_generato ASC,
                         nome_prodotto
                LIMIT ?
                """;

        return findProdottiPeriodo(dataDaInclusa, dataAEsclusa, limit, sql);
    }

    private List<ProdottoStatisticaResponse> findProdottiPeriodo(LocalDate dataDaInclusa,
                                                                 LocalDate dataAEsclusa,
                                                                 int limit,
                                                                 String sql) throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setPeriodParameters(statement, dataDaInclusa, dataAEsclusa);
            statement.setInt(3, Math.max(1, limit));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProdottoStatisticaResponse> prodotti = new ArrayList<>();
                while (resultSet.next()) {
                    prodotti.add(new ProdottoStatisticaResponse(
                            resultSet.getLong("id_prodotto"),
                            resultSet.getString("nome_prodotto"),
                            resultSet.getInt("quantita_ordinata"),
                            resultSet.getBigDecimal("ricavo_generato")
                    ));
                }
                return prodotti;
            }
        }
    }

    private void setPeriodParameters(PreparedStatement statement,
                                     LocalDate dataDaInclusa,
                                     LocalDate dataAEsclusa) throws SQLException {
        statement.setTimestamp(1, Timestamp.valueOf(dataDaInclusa.atStartOfDay()));
        statement.setTimestamp(2, Timestamp.valueOf(dataAEsclusa.atStartOfDay()));
    }
}
