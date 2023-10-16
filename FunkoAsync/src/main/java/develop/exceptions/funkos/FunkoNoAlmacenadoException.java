package develop.exceptions.funkos;

/**
 * La clase FunkoNoAlmacenadoException es una clase que extiende FunkoException
 * y se utiliza para representar excepciones relacionadas con el almacenamiento fallido de un Funko en el repositorio.
 */
public class FunkoNoAlmacenadoException extends FunkoException {

    /**
     * Construye una instancia de FunkoNoAlmacenadoException con un mensaje descriptivo.
     *
     * @param message El mensaje descriptivo de la excepción.
     */
    public FunkoNoAlmacenadoException(String message) {
        super(message);
    }
}