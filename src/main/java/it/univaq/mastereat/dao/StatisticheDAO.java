package it.univaq.mastereat.dao;

import it.univaq.mastereat.dto.web.owner.OwnerStatisticheResponse.ProdottoStatisticaResponse;
import it.univaq.mastereat.dto.web.owner.OwnerStatisticheResponse.RiepilogoStatisticheResponse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface StatisticheDAO {

    RiepilogoStatisticheResponse getRiepilogoOrdini(LocalDate dataDaInclusa,
                                                    LocalDate dataAEsclusa) throws SQLException;

    List<ProdottoStatisticaResponse> findProdottiPiuOrdinati(LocalDate dataDaInclusa,
                                                             LocalDate dataAEsclusa,
                                                             int limit) throws SQLException;

    List<ProdottoStatisticaResponse> findProdottiMenoOrdinati(LocalDate dataDaInclusa,
                                                              LocalDate dataAEsclusa,
                                                              int limit) throws SQLException;
}
