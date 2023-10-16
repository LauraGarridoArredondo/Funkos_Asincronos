package develop.services.funkos;


import develop.models.Funko;
import develop.services.cache.Cache;

/**
 * La interfaz FunkoCache extiende la interfaz Cache y proporciona métodos para almacenar, recuperar, eliminar y gestionar objetos Funko en una caché.
 */
public interface FunkoCache extends Cache<Long, Funko> {
}