package develop.services.funkos;

import develop.exceptions.funkos.FunkoNoAlmacenadoException;
import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.models.Funko;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * La interfaz FunkosService define métodos para la gestión de objetos Funko de forma asíncrona.
 */
public interface FunkosService {

    /**
     * Recupera todos los objetos Funko disponibles en el repositorio.
     *
     * @return Un CompletableFuture que representa la operación de recuperación de todos los Funkos.
     * @throws SQLException               Si ocurre un error de SQL.
     * @throws ExecutionException         Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException       Si la ejecución se ve interrumpida.
     */
    CompletableFuture <List<Funko>> findAll() throws SQLException, ExecutionException, InterruptedException;

    /**
     * Recupera todos los objetos Funko que coinciden con un nombre específico en el repositorio.
     *
     * @param nombre El nombre a buscar.
     * @return Un CompletableFuture que representa la operación de recuperación de Funkos por nombre.
     * @throws SQLException               Si ocurre un error de SQL.
     * @throws ExecutionException         Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException       Si la ejecución se ve interrumpida.
     * @throws FunkoNoEncotradoException  Si no se encuentra ningún Funko con el nombre especificado.
     */
    CompletableFuture <List<Funko>> findAllByNombre(String nombre) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException;

    /**
     * Recupera un objeto Funko por su ID en el repositorio o en la cache.
     *
     * @param id El ID del Funko a buscar.
     * @return Un CompletableFuture que representa la operación de recuperación de un Funko por ID (puede contener un valor opcional).
     * @throws SQLException               Si ocurre un error de SQL.
     * @throws ExecutionException         Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException       Si la ejecución se ve interrumpida.
     * @throws FunkoNoEncotradoException  Si no se encuentra ningún Funko con el ID especificado.
     */
    CompletableFuture <Optional<Funko>> findById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException;

    /**
     * Guarda un nuevo objeto Funko en el repositorio y en la cache.
     *
     * @param alumno El Funko a guardar.
     * @return Un CompletableFuture que representa la operación de guardado del Funko.
     * @throws SQLException               Si ocurre un error de SQL.
     * @throws ExecutionException         Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException       Si la ejecución se ve interrumpida.
     * @throws FunkoNoAlmacenadoException  Si el Funko no se puede almacenar.
     */
    CompletableFuture <Funko> save(Funko alumno) throws SQLException, ExecutionException, InterruptedException, FunkoNoAlmacenadoException;

    /**
     * Actualiza un objeto Funko existente en el repositorio y en la cache.
     *
     * @param alumno El Funko a actualizar.
     * @return Un CompletableFuture que representa la operación de actualización del Funko.
     * @throws SQLException               Si ocurre un error de SQL.
     * @throws FunkoNoEncotradoException  Si no se encuentra ningún Funko con el ID especificado.
     * @throws ExecutionException         Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException       Si la ejecución se ve interrumpida.
     */
    CompletableFuture <Funko> update(Funko alumno) throws SQLException, FunkoNoEncotradoException, ExecutionException, InterruptedException;

    /**
     * Elimina un objeto Funko por su ID del repositorio y de la cache.
     *
     * @param id El ID del Funko a eliminar.
     * @return Un CompletableFuture que representa la operación de eliminación del Funko.
     * @throws SQLException               Si ocurre un error de SQL.
     * @throws ExecutionException         Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException       Si la ejecución se ve interrumpida.
     * @throws FunkoNoEncotradoException  Si no se encuentra ningún Funko con el ID especificado.
     */
    CompletableFuture <Boolean> deleteById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException;

    /**
     * Elimina todos los objetos Funko del repositorio y de la cache.
     *
     * @return Un CompletableFuture que representa la operación de eliminación de todos los Funkos.
     * @throws SQLException       Si ocurre un error de SQL.
     * @throws ExecutionException Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException Si la ejecución se ve interrumpida.
     */
    CompletableFuture <Void> deleteAll() throws SQLException, ExecutionException, InterruptedException;
}