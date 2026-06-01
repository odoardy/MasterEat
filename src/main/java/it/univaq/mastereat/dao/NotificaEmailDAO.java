package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.NotificaEmail;

import java.sql.SQLException;

public interface NotificaEmailDAO {

    boolean existsDaInviareOInviata(long idOrdine, NotificaEmail.Tipo tipo) throws SQLException;

    NotificaEmail createDaInviare(long idOrdine,
                                  String emailDestinatario,
                                  NotificaEmail.Tipo tipo,
                                  String oggetto) throws SQLException;

    void marcaInviata(long idNotifica) throws SQLException;

    void marcaFallita(long idNotifica, String messaggioErrore) throws SQLException;
}
