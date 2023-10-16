package develop.repositories.funkos;

import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.models.Funko;
import develop.repositories.crud.CrudRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * La interfaz FunkosRepository extiende CrudRepository y proporciona métodos adicionales para buscar objetos Funko en un repositorio, identificados por un ID de tipo Long.
 */
public interface FunkosRepository extends CrudRepository<Funko, Long> {

    /**
     * Busca y devuelve una lista de objetos Funko por su nombre en el repositorio.
     *
     * @param nombre El nombre por el que se realizará la búsqueda.
     * @return Un CompletableFuture que representa la operación de búsqueda de objetos por nombre.
     * @throws SQLException               Si ocurre un error en la operación de búsqueda.
     * @throws FunkoNoEncotradoException  Si no se encuentran objetos Funko con el nombre especificado.
     */
    CompletableFuture<List<Funko>> findByNombre(String nombre) throws SQLException, FunkoNoEncotradoException;
}