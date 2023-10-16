package develop.services.funkos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.exceptions.storage.RutaInvalidaException;
import develop.models.Funko;
import develop.models.Model;
import develop.utils.LocalDateAdapter;
import develop.utils.LocalDateTimeAdapter;
import develop.utils.UuidAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * La clase FunkoStorageImpl implementa la interfaz FunkoStorage y proporciona una implementación de almacenamiento y recuperación de objetos Funko en diferentes formatos.
 * Utiliza el patrón Singleton para proporcionar una única instancia de la clase.
 */
public class FunkoStorageImpl implements FunkoStorage {
    private final Logger logger = LoggerFactory.getLogger(FunkoStorageImpl.class);
    private static FunkoStorageImpl instance;

    private FunkoStorageImpl() {}

    /**
     * Obtiene la instancia única de FunkoStorageImpl.
     *
     * @return La instancia única de FunkoStorageImpl.
     */
    public static synchronized FunkoStorageImpl getInstance() {
        if (instance == null) {
            instance = new FunkoStorageImpl();
        }
        return instance;
    }

    /**
     * Exporta una lista de objetos Funko a un archivo JSON.
     *
     * @param funkos La lista de objetos Funko a exportar.
     * @param file   La ruta del archivo de destino en formato JSON.
     * @return Un CompletableFuture que representa la operación de exportación.
     */
    public CompletableFuture<Void> exportJson(List<Funko> funkos, String file) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!validarRuta(file)) {
                    logger.error("Ruta de fichero invalida: " + file);
                    throw new RutaInvalidaException("Ruta de fichero invalida: " + file);
                } else {
                    String appPath = System.getProperty("user.dir");
                    String dataPath = appPath + File.separator + "data";
                    String backupFile = dataPath + File.separator + file;
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                            .registerTypeAdapter(UUID.class, new UuidAdapter())
                            .setPrettyPrinting()
                            .create();
                    String json = gson.toJson(funkos);
                    logger.debug("Escribiendo el archivo backup: " + backupFile);
                    Files.writeString(new File(backupFile).toPath(), json);
                }
            } catch (RutaInvalidaException | IOException e) {
                logger.error("Error al escribir el archivo backup");
                throw new RuntimeException(e);
            }
        });
    }

    private boolean validarRuta(String ruta) {
        String[] partes = ruta.split("\\.");
        if(partes.length > 1 && partes[partes.length - 1].equalsIgnoreCase("json")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Importa una lista de objetos Funko desde un archivo CSV.
     *
     * @return Un CompletableFuture que representa la operación de importación de objetos Funko desde un archivo CSV.
     */
    @Override
    public CompletableFuture<List<Funko>> importCsv() throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            String appPath = System.getProperty("user.dir");
            String dataPath = "data" + File.separator + "funkos.csv";
            Path filePath = Paths.get(appPath + File.separator + dataPath);

            logger.debug("Leyendo el archivo: " + filePath.toString());

            try(BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()))){
                return reader.lines().skip(1).map(lines -> Funko.getFunko(lines)).toList();
            } catch (FileNotFoundException e) {
                logger.error("No se encontro el archivo: " + filePath.toString());
                throw new RuntimeException(e);
            } catch (IOException e) {
                logger.error("Error al leer el archivo: " + filePath.toString());
                throw new RuntimeException(e);
            }
        });
    }
}