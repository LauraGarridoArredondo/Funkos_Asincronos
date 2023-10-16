package develop.exceptions.storage;

import java.io.IOException;
/**
 * La clase StorageException es una clase abstracta que extiende Exception
 * y se utiliza para representar excepciones relacionadas con el servicio de almacenamiento.
 */
public abstract class StorageException extends Exception  {

    /**
     * Construye una instancia de StorageException con un mensaje descriptivo.
     *
     * @param message El mensaje descriptivo de la excepcion.
     */
    public StorageException(String message) {
        super(message);
    }
}
