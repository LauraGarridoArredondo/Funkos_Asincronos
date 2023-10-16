package develop.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

/**
 * Clase adaptadora para la serialización y deserialización de objetos UUID en formato JSON.
 */
public class UuidAdapter extends TypeAdapter<UUID> {

    /**
     * Lee un valor JSON y lo convierte en un objeto UUID.
     *
     * @param jsonReader El lector JSON.
     * @return El objeto UUID deserializado.
     * @throws IOException Si ocurre un error durante la lectura del JSON.
     */
    @Override
    public UUID read(final JsonReader jsonReader) throws IOException {
        return UUID.fromString(jsonReader.nextString());
    }

    /**
     * Escribe un objeto UUID en formato JSON.
     *
     * @param jsonWriter El escritor JSON.
     * @param uuid El objeto UUID a serializar.
     * @throws IOException Si ocurre un error durante la escritura del JSON.
     */
    @Override
    public void write(JsonWriter jsonWriter, UUID uuid) throws IOException {
        jsonWriter.value(uuid.toString());

    }
}