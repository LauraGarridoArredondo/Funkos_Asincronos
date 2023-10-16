package services.funkos;

import develop.exceptions.funkos.FunkoNoAlmacenadoException;
import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.exceptions.storage.RutaInvalidaException;
import develop.models.Funko;
import develop.models.Model;
import develop.repositories.funkos.FunkosRepository;
import develop.services.funkos.FunkoCache;
import develop.services.funkos.FunkoStorage;
import develop.services.funkos.FunkosServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FunkoServiceImplTest {

    @Mock
    FunkosRepository repository;

    @Mock
    FunkoStorage storage;

    @Mock
    FunkoCache cache;

    @InjectMocks
    FunkosServiceImpl service;


    @Test
    void findAll() throws SQLException, ExecutionException, InterruptedException {
        // Arrange
        var funkos = List.of(
                Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build(),
                Funko.builder().COD(UUID.randomUUID()).name("Test-2").model(Model.MARVEL).price(19.99).releaseData(LocalDate.of(2021, 1, 1)).build()
        );

        // Cuando se llame al método al repositorio simulamos...
        when(repository.findAll()).thenReturn(CompletableFuture.completedFuture(funkos));

        // Act
        var result = service.findAll().get();

        // Assert
        assertAll("findAll",
                () -> assertEquals(result.size(), 2, "No se han recuperado dos funkos"),
                () -> assertEquals(result.get(0).getName(), "Test-1", "El primer funko no es el esperado"),
                () -> assertEquals(result.get(1).getName(), "Test-2", "El segundo funko no es el esperado"),
                () -> assertEquals(result.get(0).getModel(), Model.OTROS, "El modelo del primer funko no es el esperado"),
                () -> assertEquals(result.get(1).getModel(), Model.MARVEL, "El modelo del segundo funko no es el esperado"),
                () -> assertEquals(result.get(0).getPrice(), 9.99, "El precio del primer funko no es el esperado"),
                () -> assertEquals(result.get(1).getPrice(), 19.99, "El precio del segundo funko no es el esperado"),
                () -> assertEquals(result.get(0).getReleaseData(), LocalDate.of(2020, 1, 1), "La fecha de creacion del primer funko no es el esperado"),
                () -> assertEquals(result.get(1).getReleaseData(),LocalDate.of(2021, 1, 1), "La fecha de creacion del segundo funko no es el esperado")
        );

        // Comprobamos que se ha llamado al método del repositorio
        verify(repository, times(1)).findAll();
    }


    @Test
    void findAllByNombre() throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
        // Arrange
        var funkos = List.of(
                Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build()
        );

        // Cuando se llame al método al repositorio simulamos...
        when(repository.findByNombre("Test-1")).thenReturn(CompletableFuture.completedFuture(funkos));

        // Act
        var result = service.findAllByNombre("Test-1").get();


        // Assert
        assertAll("findAllByNombre",
                () -> assertEquals(result.size(), 1, "No se ha recuperado un funko"),
                () -> assertEquals(result.get(0).getName(), "Test-1", "El funko no es el esperado"),
                () -> assertEquals(result.get(0).getModel(), Model.OTROS, "El modelo del funko no es el esperado"),
                () -> assertEquals(result.get(0).getPrice(), 9.99, "El precio del funko no es el esperado"),
                () -> assertEquals(result.get(0).getReleaseData(), LocalDate.of(2020, 1, 1), "La fecha de creacion del funko no es el esperado")
        );

        // Comprobamos que se ha llamado al método del repositorio
        verify(repository, times(1)).findByNombre("Test-1");
    }

    @Test
    void findByIdRepository() throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(repository.findById(1L)).thenReturn(CompletableFuture.completedFuture(Optional.of(funko))); // Simulamos que lo devuelve del repositorio
        when(cache.get(1L)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));   // Simulamos que no lo consigue del cache
        // Act
        var result = service.findById(1L).get();


        // Assert
        assertAll("findById",
                () -> assertTrue(result.isPresent(), "El funko no es el esperado"),
                () -> assertEquals(result.get().getName(), "Test-1", "El funko no es el esperado"),
                () -> assertEquals(result.get().getModel(), Model.OTROS, "El modelo del funko no es el esperado"),
                () -> assertEquals(result.get().getPrice(), 9.99, "El precio del funko no es el esperado"),
                () -> assertEquals(result.get().getReleaseData(), LocalDate.of(2020, 1, 1), "La fecha de creacion del funko no es el esperado")
        );

        // Comprobamos que se ha llamado al método del repositorio
        verify(repository, times(1)).findById(1L);
        verify(cache, times(1)).get(1L);
    }

    @Test
    void findByIdCache() throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(cache.get(1L)).thenReturn(CompletableFuture.completedFuture(Optional.of(funko)));   // Simulamos que lo consigue del cache

        // Act
        var result = service.findById(1L).get();

        // Assert
        assertAll("findById",
                () -> assertTrue(result.isPresent(), "El funko no es el esperado"),
                () -> assertEquals(result.get().getName(), "Test-1", "El funko no es el esperado"),
                () -> assertEquals(result.get().getModel(), Model.OTROS, "El modelo del funko no es el esperado"),
                () -> assertEquals(result.get().getPrice(), 9.99, "El precio del funko no es el esperado"),
                () -> assertEquals(result.get().getReleaseData(), LocalDate.of(2020, 1, 1), "La fecha de creacion del funko no es el esperado")
        );

        // Comprobamos que se ha llamado al método del repositorio
        verify(cache, times(1)).get(1L);
    }


    @Test
    void findByIdNoExiste() throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(repository.findById(1L)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(cache.get(1L)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        // Act
        var result = service.findById(1L);

        assertFalse(result.get().isPresent(), "El funko esta presente");

        verify(repository, times(1)).findById(1L);
        verify(cache, times(1)).get(1L);
    }


    @Test
    void save() throws SQLException, ExecutionException, InterruptedException, FunkoNoAlmacenadoException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(repository.save(funko)).thenReturn(CompletableFuture.completedFuture(funko));
        // Act
        var result = service.save(funko);

        // Assert
        assertAll("save",
                () -> assertNotNull(result, "El resultado es nulo"),
                () -> assertEquals(result.get().getName(), "Test-1", "El funko no es el esperado"),
                () -> assertEquals(result.get().getModel(), Model.OTROS, "El modelo del funko no es el esperado"),
                () -> assertEquals(result.get().getPrice(), 9.99, "El precio del funko no es el esperado"),
                () -> assertEquals(result.get().getReleaseData(), LocalDate.of(2020, 1, 1), "La fecha de creacion del funko no es el esperado")
        );

        // Comprobamos que se ha llamado al método del repositorio y del cache
        verify(repository, times(1)).save(funko);
        verify(cache, times(1)).put(funko.getId(), funko);
    }


    @Test
    void update() throws SQLException, FunkoNoEncotradoException, ExecutionException, InterruptedException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(repository.update(funko)).thenReturn(CompletableFuture.completedFuture(funko));

        // Act
        var result = service.update(funko);

        // Assert
        assertAll("update",
                () -> assertNotNull(result, "El resultado es nulo"),
                () -> assertEquals(result.get().getName(), "Test-1", "El funko no es el esperado"),
                () -> assertEquals(result.get().getModel(), Model.OTROS, "El modelo del funko no es el esperado"),
                () -> assertEquals(result.get().getPrice(), 9.99, "El precio del funko no es el esperado"),
                () -> assertEquals(result.get().getReleaseData(), LocalDate.of(2020, 1, 1), "La fecha de creacion del funko no es el esperado")
        );

        // Comprobamos que se ha llamado al método del repositorio y del cache
        verify(repository, times(1)).update(funko);
        verify(cache, times(1)).put(funko.getId(), funko);
    }


    @Test
    void updateNoExiste() throws SQLException, FunkoNoEncotradoException, ExecutionException, InterruptedException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos la excepción FunkoNoEncotradoException...
        when(repository.update(funko)).thenThrow(new FunkoNoEncotradoException("Funko no encontrado con id: 99L"));

        // Act
        try {
            var result = service.update(funko);
        } catch (FunkoNoEncotradoException ex) {
            // Assert
            assertEquals(ex.getMessage(), "Funko no encontrado con id: 99L", "El mensaje de la excepción no es el esperado");
        }

        // Comprobamos que se ha llamado al método del repositorio
        verify(repository, times(1)).update(funko);
    }


    @Test
    void deleteById() throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(repository.deleteById(1L)).thenReturn(CompletableFuture.completedFuture(true));

        // Act
        var result = service.deleteById(1L);

        // Assert
        assertTrue(result.get(), "No se ha borrado el funko");

        // Comprobamos que se ha llamado al método del repositorio
        verify(repository, times(1)).deleteById(1L);
        verify(cache, times(1)).remove(1L);
    }

    @Test
    void deleteByIdNoExiste() throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(repository.deleteById(1L)).thenReturn(CompletableFuture.completedFuture(false));

        // Act
        var result = service.deleteById(1L);

        assertFalse(result.get(), "Un funko ha sido borrado");

        verify(repository, times(1)).deleteById(1L);
        verify(cache, times(0)).remove(1L);
    }


    @Test
    void deleteAll() throws SQLException, ExecutionException, InterruptedException {
        // Arrange
        var funko = Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build();

        // Cuando se llame al método al repositorio simulamos...
        when(repository.deleteAll()).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        service.deleteAll().get();

        // Comprobamos que se ha llamado al método del repositorio
        verify(repository, times(1)).deleteAll();
        verify(cache, times(1)).clear();
    }

    @Test
    void export() throws IOException, RutaInvalidaException, SQLException, ExecutionException, InterruptedException, FunkoNoAlmacenadoException {
        // Arrange
        String file = "funkos_test.json";
        var funkos = List.of(
                Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build(),
                Funko.builder().COD(UUID.randomUUID()).name("Test-2").model(Model.MARVEL).price(19.99).releaseData(LocalDate.of(2021, 1, 1)).build()
        );

        // Cuando se llame al método al repositorio simulamos...
        when(repository.findAll()).thenReturn(CompletableFuture.completedFuture(funkos));
        when(storage.exportJson(funkos, file)).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        service.export(file).join();

        // Comprobamos que se ha llamado al método del repositorio
        verify(storage, times(1)).exportJson(funkos, file);
        verify(repository, times(1)).findAll();
    }

    @Test
    void exportError() throws IOException, RutaInvalidaException, SQLException, ExecutionException, InterruptedException, FunkoNoAlmacenadoException {
        // Arrange
        String file = "funkos_test.csv";
        String expectedMessage = "Ruta de fichero invalida: " + file;
        var funkos = List.of(
                Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build(),
                Funko.builder().COD(UUID.randomUUID()).name("Test-2").model(Model.MARVEL).price(19.99).releaseData(LocalDate.of(2021, 1, 1)).build()
        );

        // Cuando se llame al método al repositorio simulamos...
        when(repository.findAll()).thenReturn(CompletableFuture.completedFuture(funkos));
        when(storage.exportJson(funkos, file)).thenThrow(new RutaInvalidaException("Ruta de fichero invalida: " + file));

        // Act
        Exception exception = assertThrows(ExecutionException.class, () -> {
            service.export(file).get();
        });

        // Assert
        assertTrue(exception.getMessage().contains(expectedMessage));

        // Comprobamos que se ha llamado al metodo
        verify(storage, times(1)).exportJson(funkos, file);
        verify(repository, times(1)).findAll();
        }

        @Test
        void importFile() throws IOException, SQLException, ExecutionException, InterruptedException {
        // Arrange
        List<Funko> funkos;
        var listaFunkos = List.of(
                    Funko.builder().COD(UUID.randomUUID()).name("Test-1").model(Model.OTROS).price(9.99).releaseData(LocalDate.of(2020, 1, 1)).build(),
                    Funko.builder().COD(UUID.randomUUID()).name("Test-2").model(Model.MARVEL).price(19.99).releaseData(LocalDate.of(2021, 1, 1)).build()
        );

        //  Cuando se llama al metodo importCsv...
        when(storage.importCsv()).thenReturn(CompletableFuture.completedFuture(listaFunkos));

        // Act
        funkos = service.importFile().get();

        // Assert
        assertAll("import",
                () -> assertTrue(funkos != null),
                () -> assertEquals(funkos.size(), listaFunkos.size(), "Las listas tienen tamanos distintos"),
                () -> assertEquals(funkos.get(0).getCOD(), listaFunkos.get(0).getCOD(), "El COD de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(1).getCOD(), listaFunkos.get(1).getCOD(), "El COD de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(0).getName(), listaFunkos.get(0).getName(), "El nombre de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(1).getName(), listaFunkos.get(1).getName(), "El nombre de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(0).getPrice(), listaFunkos.get(0).getPrice(), "El precio de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(1).getPrice(), listaFunkos.get(1).getPrice(), "El precio de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(0).getModel(), listaFunkos.get(0).getModel(), "El modelo de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(1).getModel(), listaFunkos.get(1).getModel(), "El modelo de los objetos en la lista son distintos" ),
                () -> assertEquals(funkos.get(0).getReleaseData(), listaFunkos.get(0).getReleaseData(), "La fecha de creacion de los objetos en la lista son distintos"),
                () -> assertEquals(funkos.get(1).getReleaseData(), listaFunkos.get(1).getReleaseData(), "La fecha de los objetos en la lista son distintos"));

        // Comprobamos que se ha llamado al metodo
        verify(storage, times(1)).importCsv();
    }
}

