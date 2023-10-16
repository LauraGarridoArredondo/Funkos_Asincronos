package develop.services.funkos;

import develop.models.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * La clase FunkoCacheImpl implementa la interfaz FunkoCache y proporciona una implementación de una caché para objetos Funko con un tamaño máximo.
 * Utiliza el patrón Singleton para proporcionar una única instancia de la caché.
 */
public class FunkoCacheImpl implements  FunkoCache {
    private final Logger logger = LoggerFactory.getLogger(FunkoCacheImpl.class);
    private final int maxSize;
    private final Map<Long, Funko> cache;
    private final ScheduledExecutorService cleaner;
    private static FunkoCacheImpl instance;

    /**
     * Obtiene la instancia única de FunkoCacheImpl con un tamaño máximo especificado.
     *
     * @param maxSize El tamaño máximo de la caché.
     * @return La instancia única de FunkoCacheImpl.
     */
    public static synchronized FunkoCacheImpl getInstance(int maxSize) {
        if(instance == null){
            instance = new FunkoCacheImpl(maxSize);
        }
        return instance;
    }

    /**
     * Obtiene el tamaño actual de la caché.
     *
     * @return El tamaño actual de la caché.
     */
    public int getTamano(){
        return cache.size();
    }

    private FunkoCacheImpl(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<Long, Funko>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Funko> eldest) {
                return size() > maxSize;
            }
        };
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear, 2, 2, TimeUnit.MINUTES);
    }

    /**
     * Almacena un objeto Funko en la caché asociado a una clave.
     *
     * @param key   La clave que identifica el objeto Funko.
     * @param value El objeto Funko que se va a almacenar en la caché.
     * @return Un CompletableFuture que representa la operación de almacenamiento en la caché.
     */
    @Override
    public CompletableFuture<Void> put(Long key, Funko value) {
        return CompletableFuture.runAsync(() -> {
            logger.debug("Añadiendo funko a cache con id: " + key + " y valor: " + value);
            cache.put(key, value);
        });
    }


    /**
     * Recupera un objeto Funko de la caché asociado a una clave.
     *
     * @param key La clave que identifica el objeto Funko a recuperar.
     * @return Un CompletableFuture que representa la operación de recuperación del objeto Funko (puede contener un valor opcional).
     */
    @Override
    public CompletableFuture<Optional<Funko>> get(Long key) {
        return CompletableFuture.supplyAsync(() -> {
           logger.debug("Obteniendo funko de cache con id: " + key);
           Optional<Funko> funko = Optional.ofNullable(cache.get(key));
           if(funko.isPresent() && funko!= null) {
               return funko;
           } else {
               return Optional.empty();
           }
        });
    }

    /**
     * Elimina un objeto Funko de la caché asociado a una clave.
     *
     * @param key La clave que identifica el objeto Funko a eliminar.
     * @return Un CompletableFuture que representa la operación de eliminación del objeto Funko en la caché.
     */
    @Override
    public CompletableFuture<Void> remove(Long key) {
        return CompletableFuture.runAsync(() -> {
           logger.debug("Eliminando funko de cache con id: " + key);
           cache.remove(key);
        });
    }

    /**
     * Limpia la caché eliminando objetos Funko caducados.
     *
     * @return Un CompletableFuture que representa la operación de limpieza de la caché.
     */
    @Override
    public CompletableFuture<Void> clear() {
        return CompletableFuture.runAsync(() -> {
            cache.entrySet().removeIf(entry -> {
               boolean shouldRemove = entry.getValue().getUpdatedAt().plusMinutes(2).isBefore(LocalDateTime.now());
               if (shouldRemove) {
                   logger.debug("Autoeliminando por caducidad funko de cache con id: " + entry.getKey());
               }
               return shouldRemove;
            });
        });
    }

    /**
     * Apaga y libera los recursos asociados a la caché.
     *
     * @return Un CompletableFuture que representa la operación de apagado de la caché.
     */
    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
           cleaner.shutdown();
        });
    }
}
