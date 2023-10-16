package develop.services.funkos;

import develop.exceptions.funkos.FunkoNoAlmacenadoException;
import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.exceptions.storage.RutaInvalidaException;
import develop.models.Funko;
import develop.repositories.funkos.FunkosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * La clase FunkosServiceImpl implementa la interfaz FunkosService para la gestión de objetos Funko.
 */
public class FunkosServiceImpl implements FunkosService {
    private static FunkosServiceImpl instance;
    private final FunkoCache cache;
    private final Logger logger = LoggerFactory.getLogger(FunkosServiceImpl.class);
    private final FunkosRepository funkosRepository;
    private final FunkoStorage funkoStorage;

    /**
     * Constructor privado de FunkosServiceImpl.
     *
     * @param funkosRepository El repositorio de Funkos.
     * @param funkoCache       La caché de Funkos.
     * @param funkoStorage     El almacenamiento de Funkos.
     */
    private FunkosServiceImpl(FunkosRepository funkosRepository, FunkoCache funkoCache, FunkoStorage funkoStorage) {
        this.funkosRepository = funkosRepository;
        this.cache = funkoCache;
        this.funkoStorage = funkoStorage;
    }

    /**
     * Obtiene una instancia de FunkosServiceImpl.
     *
     * @param funkosRepository El repositorio de Funkos.
     * @param funkoCache       La caché de Funkos.
     * @param funkoStorage     El almacenamiento de Funkos.
     * @return Una instancia de FunkosServiceImpl.
     */
    public static synchronized FunkosServiceImpl getInstance(FunkosRepository funkosRepository, FunkoCache funkoCache, FunkoStorage funkoStorage) {
        if (instance == null) {
            instance = new FunkosServiceImpl(funkosRepository, funkoCache, funkoStorage);
        }
        return instance;
    }

    /**
     * Obtiene una lista de todos los Funkos.
     *
     * @return Un CompletableFuture que representa la lista de Funkos.
     * @throws SQLException       Si ocurre un error de SQL.
     * @throws ExecutionException Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException Si la ejecución se ve interrumpida.
     */
    @Override
    public CompletableFuture <List<Funko>> findAll() throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Obteniendo todos los funkos");
        return funkosRepository.findAll();
    }

    /**
     * Obtiene una lista de Funkos por nombre.
     *
     * @param nombre El nombre de los Funkos a buscar.
     * @return Un CompletableFuture que representa la lista de Funkos encontrados.
     * @throws SQLException              Si ocurre un error de SQL.
     * @throws ExecutionException        Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException        Si la ejecución se ve interrumpida.
     * @throws FunkoNoEncotradoException Si no se encuentran Funkos con el nombre especificado.
     */
    @Override
    public CompletableFuture <List<Funko>> findAllByNombre(String nombre) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
           logger.debug("Obteniendo todos los funkos con nombre: " + nombre);
           return funkosRepository.findByNombre(nombre);
    }

    /**
     * Obtiene un Funko por su ID.
     *
     * @param id El ID del Funko a buscar.
     * @return Un CompletableFuture que representa el Funko encontrado (si existe).
     * @throws SQLException              Si ocurre un error de SQL.
     * @throws ExecutionException        Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException        Si la ejecución se ve interrumpida.
     * @throws FunkoNoEncotradoException Si el Funko no se encuentra.
     */
    @Override
    public CompletableFuture <Optional<Funko>> findById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
            logger.debug("Obteniendo el funko con id: " + id);
            CompletableFuture<Optional<Funko>> funko = cache.get(id);
            if(funko.get().isPresent()) {
                logger.debug("Funko encontrado en cache");
                return funko;
            } else {
                logger.debug("Funko no encontrado en cache\nBuscando en la base de datos");
                return funkosRepository.findById(id);
            }
    }

    /**
     * Guarda un Funko en la base de datos y en la caché.
     *
     * @param funko El Funko a guardar.
     * @return Un CompletableFuture que representa el Funko guardado.
     * @throws SQLException              Si ocurre un error de SQL.
     * @throws ExecutionException        Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException        Si la ejecución se ve interrumpida.
     * @throws FunkoNoAlmacenadoException Si no se puede almacenar el Funko.
     */
    @Override
    public CompletableFuture <Funko> save(Funko funko) throws SQLException, ExecutionException, InterruptedException, FunkoNoAlmacenadoException {
            logger.debug("Guardando funko "+ funko);
            CompletableFuture<Funko> funkoGuardado = funkosRepository.save(funko);
            cache.put(funkoGuardado.get().getId(), funkoGuardado.get());
            return funkoGuardado;
    }

    /**
     * Actualiza un Funko en la base de datos y en la caché.
     *
     * @param funko El Funko a actualizar.
     * @return Un CompletableFuture que representa el Funko actualizado.
     * @throws SQLException              Si ocurre un error de SQL.
     * @throws ExecutionException        Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException        Si la ejecución se ve interrumpida.
     * @throws FunkoNoEncotradoException Si el Funko no se encuentra.
     */
    @Override
    public CompletableFuture<Funko> update(Funko funko) throws SQLException, FunkoNoEncotradoException, ExecutionException, InterruptedException {
            logger.debug("Actualizando funko: " + funko);
            cache.put(funko.getId(), funko);
            return funkosRepository.update(funko);
    }

    /**
     * Borra un Funko por su ID de la base de datos y de la caché.
     *
     * @param id El ID del Funko a borrar.
     * @return Un CompletableFuture que representa si se logró borrar el Funko.
     * @throws SQLException              Si ocurre un error de SQL.
     * @throws ExecutionException        Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException        Si la ejecución se ve interrumpida.
     * @throws FunkoNoEncotradoException Si el Funko no se encuentra.
     */
    @Override
    public CompletableFuture<Boolean> deleteById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
            logger.debug("Borrando funko con id: " + id);
            CompletableFuture <Boolean> deleted = funkosRepository.deleteById(id);
            if(deleted.get()) {
                cache.remove(id);
            }
            return deleted;
    }

    /**
     * Borra todos los Funkos de la base de datos y la caché.
     *
     * @return Un CompletableFuture que representa la operación de borrado.
     * @throws SQLException       Si ocurre un error de SQL.
     * @throws ExecutionException Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException Si la ejecución se ve interrumpida.
     */
    @Override
    public CompletableFuture <Void> deleteAll() throws SQLException, ExecutionException, InterruptedException {
            logger.debug("Borrando todos los funkos");
            cache.clear();
            return funkosRepository.deleteAll();
    }

    /**
     * Exporta los Funkos a un archivo JSON.
     *
     * @param file El nombre del archivo de salida.
     * @return Un CompletableFuture que representa la operación de exportación.
     * @throws SQLException       Si ocurre un error de SQL.
     * @throws IOException        Si ocurre un error de E/S.
     * @throws ExecutionException Si ocurre una excepción durante la ejecución.
     * @throws InterruptedException Si la ejecución se ve interrumpida.
     * @throws RutaInvalidaException Si la ruta especificada es inválida.
     */
    public CompletableFuture <Void> export(String file) throws SQLException, IOException, ExecutionException, InterruptedException, RutaInvalidaException {
            return CompletableFuture.runAsync(() -> {
                logger.debug("Guardando funkos en archivo");
                try {
                    funkoStorage.exportJson(this.findAll().get(), file);
                } catch (IOException | SQLException | ExecutionException | InterruptedException | RutaInvalidaException e) {
                    logger.error("Error al guardar los funkos en archivo JSON");
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * Importa una lista de Funkos desde un archivo CSV.
     *
     * @return Un CompletableFuture que representa la lista de Funkos importados.
     * @throws SQLException       Si ocurre un error de SQL.
     * @throws IOException        Si ocurre un error de E/S.
     */
    public CompletableFuture <List<Funko>> importFile() throws SQLException, IOException {
            logger.debug("Importando funkos de archivo CSV");
            return funkoStorage.importCsv();
    }
}