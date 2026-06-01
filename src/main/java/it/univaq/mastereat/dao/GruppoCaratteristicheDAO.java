package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.GruppoCaratteristiche;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GruppoCaratteristicheDAO {

    List<GruppoCaratteristiche> findAllByProdottoIdForOwner(int idProdotto) throws SQLException;

    List<GruppoCaratteristiche> findActiveByProdottoId(int idProdotto) throws SQLException;

    Optional<GruppoCaratteristiche> findByProdottoIdAndIdForOwner(int idProdotto,
                                                                  long idGruppoCaratteristiche) throws SQLException;

    boolean existsActiveByProdottoIdAndId(int idProdotto,
                                          long idGruppoCaratteristiche) throws SQLException;

    boolean existsActiveByProdottoIdAndNome(int idProdotto,
                                            String nome,
                                            Long idGruppoDaEscludere) throws SQLException;

    boolean existsActiveCaratteristicheByGruppoForOwner(int idProdotto,
                                                        long idGruppoCaratteristiche) throws SQLException;

    GruppoCaratteristiche createForOwner(int idProdotto,
                                         String nome,
                                         String descrizione,
                                         boolean obbligatorio,
                                         boolean attivo) throws SQLException;

    Optional<GruppoCaratteristiche> updateForOwner(int idProdotto,
                                                   long idGruppoCaratteristiche,
                                                   String nome,
                                                   String descrizione,
                                                   boolean obbligatorio,
                                                   boolean attivo) throws SQLException;

    boolean deactivateForOwner(int idProdotto, long idGruppoCaratteristiche) throws SQLException;
}
