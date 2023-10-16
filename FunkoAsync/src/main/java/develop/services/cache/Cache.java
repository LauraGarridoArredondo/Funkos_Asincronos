package develop.services.cache;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * La interfaz Cache proporciona métodos para almacenar, recuperar, eliminar y gestionar elementos en una caché.
 *
 * @param <K> El tipo de clave para los elementos de la caché.
 * @param <V> El tipo de valor para los elementos de la caché.
 */
public interface Cache<K, V> {

    /**
     * Almacena un elemento en la caché asociado a una clave.
     *
     * @param key   La clave que identifica el elemento.
     * @param value El valor que se va a almacenar en la caché.
     * @return Un CompletableFuture que representa la operación de almacenamiento.
     */
    CompletableFuture <Void> put(K key, V value);

    /**
     * Recupera un elemento de la cache asociado a una clave.
     *
     * @param key La clave que identifica el elemento a recuperar.
     * @return Un CompletableFuture que representa la operacion de recuperacion del elemento (puede contener un valor opcional).
     */
    CompletableFuture  <Optional<V>> get(K key);

    /**
     * Elimina un elemento de la caché asociado a una clave.
     *
     * @param key La clave que identifica el elemento a eliminar.
     * @return Un CompletableFuture que representa la operación de eliminación del elemento.
     */
    CompletableFuture <Void> remove(K key);

    /**
     * Limpia todos los elementos de la caché que tengan 2 min desde la ultima vez que fueron accedidos.
     *
     * @return Un CompletableFuture que representa la operación de limpieza de la caché.
     */
    CompletableFuture <Void> clear();


    /**
     * Apaga y libera los recursos asociados a la caché.
     *
     * @return Un CompletableFuture que representa la operación de apagado de la caché.
     */
    CompletableFuture <Void> shutdown();
}