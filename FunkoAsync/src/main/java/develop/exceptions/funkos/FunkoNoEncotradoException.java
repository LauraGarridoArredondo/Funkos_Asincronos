package develop.exceptions.funkos;

/**
 * La clase FunkoNoEncontradoException es una clase que extiende FunkoException
 * y se utiliza para representar excepciones relacionadas con la no existencia de un Funko en el repositorio.
 */
public class FunkoNoEncotradoException extends FunkoException {

    /**
     * Construye una instancia de FunkoNoEncontradoException con un mensaje descriptivo.
     *
     * @param message El mensaje descriptivo de la excepción.
     */
    public FunkoNoEncotradoException(String message) {
        super(message);
    }
}