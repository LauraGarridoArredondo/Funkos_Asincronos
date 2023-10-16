package develop.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Clase adaptadora para la serialización y deserialización de objetos LocalDateTime en formato JSON.
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    /**
     * Lee un valor JSON y lo convierte en un objeto LocalDateTime.
     *
     * @param jsonReader El lector JSON.
     * @return El objeto LocalDateTime deserializado.
     * @throws IOException Si ocurre un error durante la lectura del JSON.
     */
    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        return LocalDateTime.parse(jsonReader.nextString());
    }


    /**
     * Escribe un objeto LocalDateTime en formato JSON.
     *
     * @param jsonWriter El escritor JSON.
     * @param localDateTime El objeto LocalDateTime a serializar.
     * @throws IOException Si ocurre un error durante la escritura del JSON.
     */
    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        jsonWriter.value(localDateTime.toString());

    }
}