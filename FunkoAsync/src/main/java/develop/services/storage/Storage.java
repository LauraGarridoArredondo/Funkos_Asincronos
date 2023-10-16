package develop.services.storage;


import develop.exceptions.storage.RutaInvalidaException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * La interfaz Storage proporciona métodos para exportar datos a un archivo JSON y para importar datos desde un archivo CSV.
 *
 * @param <T> El tipo de datos que se almacena y gestiona.
 */
public interface Storage<T> {

    /**
     * Exporta una lista de datos a un archivo JSON.
     *
     * @param data La lista de datos que se va a exportar.
     * @param file La ruta del archivo JSON de destino.
     * @return Un CompletableFuture que representa la operación de exportación a JSON.
     * @throws IOException          Si ocurre un error de E/S durante la operación de exportación.
     * @throws RutaInvalidaException Si la ruta del archivo es inválida.
     */
    CompletableFuture<Void> exportJson(List<T> data, String file) throws IOException, RutaInvalidaException;

    /**
     * Importa datos desde un archivo CSV y los devuelve como una lista.
     *
     * @return Un CompletableFuture que representa la operación de importación desde CSV (devuelve una lista de datos).
     * @throws IOException Si ocurre un error de E/S durante la operación de importación.
     */
    CompletableFuture <List<T>> importCsv() throws IOException;
}