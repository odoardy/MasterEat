package it.univaq.mastereat.dao.impl;

import it.univaq.mastereat.dao.CategoriaProdottoDAO;
import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import it.univaq.mastereat.model.CategoriaProdotto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaProdottoDAOImpl implements CategoriaProdottoDAO {

    @Override
    public List<CategoriaProdotto> findAllActive() throws SQLException {
        String sql = """
                SELECT id,
                       nome,
                       descrizione,
                       ordine_visualizzazione
                FROM categorie_prodotto
                WHERE attiva = TRUE
                ORDER BY ordine_visualizzazione, nome
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<CategoriaProdotto> categorie = new ArrayList<>();
            while (resultSet.next()) {
                categorie.add(mapRow(resultSet));
            }
            return categorie;
        }
    }

    @Override
    public boolean existsActiveById(long idCategoria) throws SQLException {
        String sql = """
                SELECT 1
                FROM categorie_prodotto
                WHERE id = ?
                  AND attiva = TRUE
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idCategoria);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private CategoriaProdotto mapRow(ResultSet resultSet) throws SQLException {
        return new CategoriaProdotto(
                resultSet.getLong("id"),
                resultSet.getString("nome"),
                resultSet.getString("descrizione"),
                resultSet.getInt("ordine_visualizzazione")
        );
    }
}
