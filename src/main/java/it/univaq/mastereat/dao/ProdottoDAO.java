package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.Prodotto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProdottoDAO {

    List<Prodotto> findAll() throws SQLException;

    List<Prodotto> findAllForOwner() throws SQLException;

    Prodotto createForOwner(Long idCategoria,
                            String nome,
                            String descrizione,
                            BigDecimal prezzoBase,
                            int minutiPreparazione,
                            String descrizionePreparazione,
                            boolean attivo) throws SQLException;

    Optional<Prodotto> findById(int id) throws SQLException;

    Optional<Prodotto> findByIdForOwner(int id) throws SQLException;

    Optional<Prodotto> updateForOwner(int id,
                                      Long idCategoria,
                                      String nome,
                                      String descrizione,
                                      BigDecimal prezzoBase,
                                      int minutiPreparazione,
                                      String descrizionePreparazione,
                                      boolean attivo) throws SQLException;

    List<Prodotto> search(String nome, BigDecimal prezzoMin, BigDecimal prezzoMax) throws SQLException;
}
