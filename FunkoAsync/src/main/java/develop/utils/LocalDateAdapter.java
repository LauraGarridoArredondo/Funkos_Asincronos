package develop.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Clase adaptadora para la serialización y deserialización de objetos LocalDate en formato JSON.
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    /**
     * Lee un valor JSON y lo convierte en un objeto LocalDate.
     *
     * @param jsonReader El lector JSON.
     * @return El objeto LocalDate deserializado.
     * @throws IOException Si ocurre un error durante la lectura del JSON.
     */

    @Override
    public LocalDate read(final JsonReader jsonReader) throws IOException {
        return LocalDate.parse(jsonReader.nextString());
    }

    /**
     * Escribe un objeto LocalDate en formato JSON.
     *
     * @param jsonWriter El escritor JSON.
     * @param localDate El objeto LocalDate a serializar.
     * @throws IOException Si ocurre un error durante la escritura del JSON.
     */
    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
        jsonWriter.value(localDate.toString());

    }
}