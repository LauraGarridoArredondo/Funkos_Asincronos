package services.funkos;

import develop.exceptions.storage.RutaInvalidaException;
import develop.models.Funko;
import develop.models.IdGenerator;
import develop.models.Model;
import develop.repositories.funkos.FunkosRepository;
import develop.repositories.funkos.FunkosRepositoryImpl;
import develop.services.database.DatabaseManager;
import develop.services.funkos.FunkoStorage;
import develop.services.funkos.FunkoStorageImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class FunkoStorageTest {
    private FunkoStorage funkosStorage;

    @BeforeEach
    void setUp() throws SQLException {
        funkosStorage = FunkoStorageImpl.getInstance();
    }

    @Test
    void importFunkosCsv() throws IOException, ExecutionException, InterruptedException {
        List<Funko> funkos = funkosStorage.importCsv().get(); // Importamos todos los funkos desde el CSV preestablecido
        assertAll(() -> assertNotNull(funkos), // Comprobamos que la lista no sea nula
                () -> assertTrue(funkos.size() > 0), // Comprobamos que la lista tenga al menos un elemento
                () -> assertEquals(90, funkos.size() ) // Sabiendo que la lista tiene 90 elementos, comprobamos que el tamano sea el correcto
        );
    }

    @Test
    void exportFunkosJson() throws IOException, RutaInvalidaException, InterruptedException, ExecutionException {
        String archivoEsperado = System.getProperty("user.dir") + File.separator + "data" + File.separator + "funkos_test.json"; // Creamos la ruta esperada
        File f = new File(archivoEsperado); // Creamos el objeto file

        List<Funko> funkos = new ArrayList<>(); // Creamos la lista de funkos
        Funko funko1 = Funko.builder()
                .id(1L)
                .COD(UUID.randomUUID())
                .name("Test-1")
                .model(Model.OTROS)
                .price(19.99)
                .releaseData(LocalDate.of(2020, 1, 1))
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        Funko funko2 = Funko.builder()
                .id(2L)
                .COD(UUID.randomUUID())
                .name("Test-2")
                .model(Model.ANIME)
                .price(99.99)
                .releaseData(LocalDate.of(2020, 4, 1))
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        funkos.add(funko1); // Añadiendo funko1 a la lista
        funkos.add(funko2); // Añadiendo funko2 a la lista

        funkosStorage.exportJson(funkos, "funkos_test.json").get(); // Exportamos todos los funkos de la lista en un archivo json pasandole el nombre del archivo

        assertAll(() -> assertTrue(f.exists())); // Comprobamos que el archivo json se ha creado

        Thread.sleep(10000); // Esperamos 10 segundo
        f.delete(); // Eliminamos el archivo json
    }

    @Test
    void exportFunkosJsonInvalido(){
        String rutaInvalida = "funkos_prueba.csv";

        List<Funko> funkos = new ArrayList<>(); // Creamos la lista de funkos
        Funko funko1 = Funko.builder()
                .id(1L)
                .COD(UUID.randomUUID())
                .name("Test-1")
                .model(Model.OTROS)
                .price(19.99)
                .releaseData(LocalDate.of(2020, 1, 1))
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        Funko funko2 = Funko.builder()
                .id(2L)
                .COD(UUID.randomUUID())
                .name("Test-2")
                .model(Model.ANIME)
                .price(99.99)
                .releaseData(LocalDate.of(2020, 4, 1))
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        funkos.add(funko1); // Añadiendo funko1 a la lista
        funkos.add(funko2); // Añadiendo funko2 a la lista

        Exception exception = assertThrows(ExecutionException.class, () -> {
            funkosStorage.exportJson(funkos, rutaInvalida).get();
        });
        String expectedMessage = "Ruta de fichero invalida: "+ rutaInvalida;
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
