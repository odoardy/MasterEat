package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.Caratteristica;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CaratteristicaDAO {

    List<Caratteristica> findByProdottoId(int idProdotto) throws SQLException;

    List<Caratteristica> findAllByProdottoIdForOwner(int idProdotto) throws SQLException;

    Caratteristica createForOwner(int idProdotto,
                                  Long idGruppoCaratteristiche,
                                  String nome,
                                  String descrizione,
                                  BigDecimal differenzaPrezzo,
                                  boolean selezionataDefault,
                                  boolean attiva) throws SQLException;

    Map<Long, List<Caratteristica>> findByProdottoIds(List<Long> idProdotti) throws SQLException;

    Optional<Caratteristica> findByProdottoIdAndIdForOwner(int idProdotto, int idCaratteristica) throws SQLException;

    Optional<Caratteristica> updateForOwner(int idProdotto,
                                            int idCaratteristica,
                                            Long idGruppoCaratteristiche,
                                            String nome,
                                            String descrizione,
                                            BigDecimal differenzaPrezzo,
                                            boolean selezionataDefault,
                                            boolean attiva) throws SQLException;

    boolean deleteFromProdotto(int idProdotto, int idCaratteristica) throws SQLException;

    boolean existsOtherDefaultInGroupForOwner(int idProdotto,
                                              long idGruppoCaratteristiche,
                                              Long idCaratteristicaDaEscludere) throws SQLException;
}
