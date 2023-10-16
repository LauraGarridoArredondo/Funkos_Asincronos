package develop.repositories.crud;


import develop.exceptions.funkos.FunkoNoAlmacenadoException;
import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.models.Funko;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * La interfaz CrudRepository proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Borrar) en una colección de objetos de tipo T, identificados por un ID de tipo ID.
 *
 * @param <T>  El tipo de objeto que se va a almacenar y gestionar.
 * @param <ID> El tipo de identificador único para los objetos.
 */
public interface CrudRepository<T, ID> {

    /**
     * Guarda un objeto de tipo T en el repositorio.
     *
     * @param t El objeto que se va a guardar.
     * @return Un CompletableFuture que representa la operación de guardado.
     * @throws SQLException              Si ocurre un error en la operación de guardado.
     * @throws FunkoNoAlmacenadoException Si el objeto Funko no se almacena correctamente.
     */
    CompletableFuture<Funko> save(T t) throws SQLException, FunkoNoAlmacenadoException;

    /**
     * Actualiza un objeto de tipo T en el repositorio.
     *
     * @param t El objeto que se va a actualizar.
     * @return Un CompletableFuture que representa la operación de actualización.
     * @throws SQLException               Si ocurre un error en la operación de actualización.
     * @throws FunkoNoEncotradoException  Si el objeto Funko no se encuentra para ser actualizado.
     */
    CompletableFuture<Funko> update(T t) throws SQLException, FunkoNoEncotradoException;


    /**
     * Busca un objeto por su ID en el repositorio.
     *
     * @param id El ID del objeto que se busca.
     * @return Un CompletableFuture que representa la operación de búsqueda.
     * @throws SQLException               Si ocurre un error en la operación de búsqueda.
     * @throws FunkoNoEncotradoException  Si el objeto Funko no se encuentra.
     */
    CompletableFuture<Optional<Funko>> findById(ID id) throws SQLException, FunkoNoEncotradoException;

    /**
     * Busca y devuelve todos los objetos del repositorio.
     *
     * @return Un CompletableFuture que representa la operación de búsqueda de todos los objetos.
     * @throws SQLException Si ocurre un error en la operación de búsqueda.
     */
    CompletableFuture<List<Funko>> findAll() throws SQLException;

    /**
     * Borra un objeto por su ID en el repositorio.
     *
     * @param id El ID del objeto que se va a borrar.
     * @return Un CompletableFuture que representa la operación de borrado.
     * @throws SQLException               Si ocurre un error en la operación de borrado.
     * @throws FunkoNoEncotradoException  Si el objeto Funko no se encuentra para ser borrado.
     */
    CompletableFuture<Boolean> deleteById(ID id) throws SQLException, FunkoNoEncotradoException;

    /**
     * Borra todos los objetos del repositorio.
     *
     * @return Un CompletableFuture que representa la operación de borrado de todos los objetos.
     * @throws SQLException Si ocurre un error en la operación de borrado.
     */
    CompletableFuture<Void> deleteAll() throws SQLException;
}