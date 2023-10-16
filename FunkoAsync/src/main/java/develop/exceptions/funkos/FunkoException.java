package develop.exceptions.funkos;

/**
 * La clase FunkoException es una clase abstracta que extiende Exception y se utiliza
 * para representar excepciones relacionadas con Funko en el repositorio.
 */
public abstract class FunkoException extends Exception {

    /**
     * Construye una instancia de FunkoException con un mensaje descriptivo.
     *
     * @param message El mensaje descriptivo de la excepción.
     */
    public FunkoException(String message) {
        super(message);
    }
}
