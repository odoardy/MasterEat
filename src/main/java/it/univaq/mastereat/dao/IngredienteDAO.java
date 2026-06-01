package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.Ingrediente;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IngredienteDAO {

    List<Ingrediente> findByProdottoId(int idProdotto) throws SQLException;

    List<Ingrediente> findAllByProdottoIdForOwner(int idProdotto) throws SQLException;

    List<Ingrediente> findAllActiveForOwner() throws SQLException;

    Optional<Ingrediente> findByIdForOwner(long idIngrediente) throws SQLException;

    Optional<Ingrediente> findActiveByIdForOwner(long idIngrediente) throws SQLException;

    Optional<Ingrediente> findByProdottoIdAndIdForOwner(int idProdotto, long idIngrediente) throws SQLException;

    boolean existsAssociationForOwner(int idProdotto, long idIngrediente) throws SQLException;

    Ingrediente createForOwner(String nome,
                               String unitaMisura,
                               boolean allergene,
                               boolean attivo) throws SQLException;

    Ingrediente associateForOwner(int idProdotto,
                                  long idIngrediente,
                                  BigDecimal quantita) throws SQLException;

    Optional<Ingrediente> updateForOwner(int idProdotto,
                                         long idIngrediente,
                                         String nome,
                                         String unitaMisura,
                                         BigDecimal quantita,
                                         boolean allergene,
                                         boolean attivo) throws SQLException;

    boolean removeFromProdottoForOwner(int idProdotto, long idIngrediente) throws SQLException;
}
