package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.ImmagineProdotto;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ImmagineProdottoDAO {

    List<ImmagineProdotto> findByProdottoId(int idProdotto) throws SQLException;

    Map<Long, List<ImmagineProdotto>> findByProdottoIds(List<Long> idProdotti) throws SQLException;

    Optional<ImmagineProdotto> findByProdottoIdAndId(int idProdotto, long idImmagine) throws SQLException;

    long countByProdottoId(int idProdotto) throws SQLException;

    ImmagineProdotto create(int idProdotto,
                            String nomeFileOriginale,
                            String nomeFileSalvato,
                            String percorsoFile,
                            String tipoContenuto,
                            long dimensioneByte,
                            String testoAlternativo,
                            int ordineVisualizzazione,
                            boolean principale) throws SQLException;

    boolean setPrincipale(int idProdotto, long idImmagine) throws SQLException;

    Optional<ImmagineProdotto> deleteByProdottoIdAndId(int idProdotto, long idImmagine) throws SQLException;
}
