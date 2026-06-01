package it.univaq.mastereat.dao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Factory centralizzata per le connessioni JDBC.
 *
 * La webapp recupera il DataSource configurato in Tomcat tramite JNDI
 * {@code java:comp/env/jdbc/MasterEatDB}, evitando che controller e service
 * conoscano i dettagli di configurazione del database.
 */
public final class DatabaseConnectionFactory {

    private static final String JNDI_DATASOURCE_NAME = "java:comp/env/jdbc/MasterEatDB";

    private static volatile DataSource dataSource;

    private DatabaseConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static DataSource getDataSource() throws SQLException {
        DataSource currentDataSource = dataSource;
        if (currentDataSource != null) {
            return currentDataSource;
        }

        synchronized (DatabaseConnectionFactory.class) {
            if (dataSource == null) {
                dataSource = lookupDataSource();
            }
            return dataSource;
        }
    }

    private static DataSource lookupDataSource() throws SQLException {
        try {
            Object resource = new InitialContext().lookup(JNDI_DATASOURCE_NAME);
            if (resource instanceof DataSource resolvedDataSource) {
                return resolvedDataSource;
            }
            throw new SQLException("La risorsa JNDI " + JNDI_DATASOURCE_NAME + " non e un DataSource");
        } catch (NamingException exception) {
            throw new SQLException("DataSource JNDI non trovato: " + JNDI_DATASOURCE_NAME, exception);
        }
    }
}
