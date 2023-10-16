package develop.exceptions.storage;

/**
 * La clase RutaInvalidaException es una clase que extiende StorageException
 * y se utiliza para representar excepciones relacionadas con una ruta inválida en el servicio de almacenamiento.
 */
public class RutaInvalidaException extends StorageException {

    /**
     * Construye una instancia de RutaInvalidaException con un mensaje descriptivo.
     *
     * @param message El mensaje descriptivo de la excepción.
     */
    public RutaInvalidaException(String message) {
        super(message);
    }
}