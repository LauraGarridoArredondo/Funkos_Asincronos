package develop.models;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * La clase IdGenerator proporciona una forma de generar y administrar identificadores unicos.
 * Se utiliza un patrón Singleton para garantizar una unica instancia de generador de identificadores.
 */
public class IdGenerator {
    private static IdGenerator instance;
    private static long id = 0;
    private final Lock locker = new ReentrantLock(true);

    /**
     * Metodo estatico para obtener la instancia unica del generador de identificadores.
     *
     * @return La instancia única del generador de identificadores.
     */
    public static synchronized IdGenerator getInstance(){
        if(instance == null){
           instance = new IdGenerator();
        }
        return instance;
    }

    /**
     * Genera un identificador unico y lo incrementa.
     *
     * @return Un identificador unico generado.
     */
    public Long getIdAndIncrement() {
        Long idCopia = 0L;
        locker.lock();
        id++;
        idCopia = id;
        locker.unlock();
        return idCopia;
    }

    /**
     * Reinicia el identificador a su valor inicial (0).
     */
    public void resetId() {
        this.id = 0L;
    }
}
