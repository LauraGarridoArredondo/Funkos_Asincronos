package develop.models;

import develop.locale.MyLocale;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * La clase Funko representa una figura de colección, a menudo asociada con la cultura popular.
 * Esta clase proporciona un patrón de creación con el constructor de objetos para crear instancias de Funko y
 * incluye varios atributos, como un ID, código único, nombre, modelo, precio, fecha de lanzamiento, marca de tiempo de creación y marca de tiempo de última actualización.
 */
@Data
@Builder
public class Funko {
    private long id; // Identificador unico del funko, este lo asigna la base de datos
    private UUID COD; // Codigo unico del funko
    private long myId; // Otro identificador que se le asigna con el IdGenerator
    private String name; // Nombre del funko
    private Model model; // Modelo del funko
    private double price; // Precio del funko
    private LocalDate releaseData; // Fecha de creacion del funko
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // Fecha de creacion del funko en la base de datos
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // Fecha de ultima actualizacion del funko

    /**
     *
     * Crea una representacion en cadena string de una instancia funko con sus atributos y valores
     *
     * @return Una representacion en cadena string de la instancia de un Funko con sus valores.
     */
    @Override
    public String toString() {
        MyLocale myLocal = new MyLocale();
        return "Funko{" +
                "id=" + id +
                ", COD=" + COD +
                ", myId=" + myId +
                ", name='" + name + '\'' +
                ", model=" + model +
                ", price=" + myLocal.toLocalMoney(price) +    // Imprimimos el precio codificado a la moneda Local
                ", releaseData=" + myLocal.toLocalDate(releaseData) + // Imprimimos el año de lanzamiento codificado a la fecha local
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


    /**
     * Convierte una representacion de una cadena string de un UUID a un objeto UUID.
     *
     * @param uuid La representacion de la cadena String del UUID.
     * @return El objeto UUID convertido.
     */
    public static UUID getUUID(String uuid) {
        return uuid.length() > 36? UUID.fromString(uuid.substring(0,36)): UUID.fromString(uuid);
    }

    /**
     * Convierte una representación de cadena de una fecha a un objeto LocalDate con formato "yyyy-MM-dd".
     *
     * @param date La representación de cadena de la fecha.
     * @return Un objeto LocalDate convertido.
     */
    public static LocalDate getDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date,formatter);
    }

    /**
     * Convierte una representacion de cadena String de un funko a un objeto Funko, separandola por campo y extrayendo sus atributos en orden: codigo, nombre, modelo, precio y fecha de lanzamiento
     *
     * @param linea La representacion de Funko en cadena String.
     * @return Una instancia de Funko construida a partir de los datos proporcionados.
     */
    public static Funko getFunko(String linea){
        String [] campos = linea.split(",");
        UUID COD = getUUID(campos[0]);
        String name = campos[1];
        Model model = Model.valueOf(campos[2]);
        double price = Double.parseDouble(campos[3]);
        LocalDate releaseData = getDate(campos[4]);
        return Funko.builder()
                .COD(COD)
                .name(name)
                .model(model)
                .price(price)
                .releaseData(releaseData)
                .build();
    }
}