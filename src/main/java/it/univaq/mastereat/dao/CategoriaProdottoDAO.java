package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.CategoriaProdotto;

import java.sql.SQLException;
import java.util.List;

public interface CategoriaProdottoDAO {

    List<CategoriaProdotto> findAllActive() throws SQLException;

    boolean existsActiveById(long idCategoria) throws SQLException;
}
