package develop;

import develop.exceptions.funkos.FunkoNoAlmacenadoException;
import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.exceptions.storage.RutaInvalidaException;
import develop.locale.MyLocale;
import develop.models.Funko;
import develop.models.IdGenerator;
import develop.models.Model;
import develop.repositories.funkos.FunkosRepositoryImpl;
import develop.services.database.DatabaseManager;
import develop.services.funkos.*;


import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Metodo principal del programa
 */
public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException, SQLException, IOException, FunkoNoEncotradoException, FunkoNoAlmacenadoException {
        MyLocale myLocale = new MyLocale();
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        IdGenerator idGenerator = IdGenerator.getInstance();
        FunkosRepositoryImpl funkosRepository = FunkosRepositoryImpl.getInstance(databaseManager, idGenerator);
        FunkoCacheImpl funkoCache = FunkoCacheImpl.getInstance(10);
        FunkoStorageImpl funkoStorage = FunkoStorageImpl.getInstance();
        FunkosServiceImpl funkosService = FunkosServiceImpl.getInstance(funkosRepository, funkoCache, funkoStorage);

        // Importamos los funkos del CSV
        CompletableFuture<List<Funko>> funkosImportados = funkosService.importFile();


        // Guardamos en la base de datos los funkos importados
        funkosImportados.get().forEach(funko -> {
            try {
                funkosService.save(funko);
            } catch (SQLException | FunkoNoAlmacenadoException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        // Cogemos los funkos de la base de datos para hacer las consultas
        List<Funko> funkos = funkosService.findAll().get();

        // FUNKO MAS CARO
        double maxPrecio = funkos.stream().mapToDouble(Funko::getPrice).max().getAsDouble(); // Sacamos el precio máximo de todos los funkos
        Optional<Funko> funkoMasCaro = funkos.stream().filter(f -> f.getPrice() == maxPrecio).findFirst(); // Sacamos el funko mas caro segun el precio maximo
        System.out.println("FUNKO MAS CARO: " + funkoMasCaro);

        // MEDIA DE PRECIO DE LOS FUNKOS
        double mediaPrecio = funkos.stream().mapToDouble(Funko::getPrice).average().orElse(0.0); // Sacamos el precio medio de todos los funkos
        System.out.println("PRECIO MEDIO: " + myLocale.toLocalMoney(mediaPrecio));  // Imprimimos el precio medio codificado a la moneda Local

        // FUNKOS AGRUPADOS POR MODELO
        Map<Model, List<Funko>> funkosPorModelo = funkos.stream()    // Creamos un map donde agrupamos los funkos segun modelo
                .collect(Collectors.groupingBy(Funko::getModel));
        System.out.println("FUNKOS AGRUPADOS POR MODELO: ");
        funkosPorModelo.forEach((a, b) -> System.out.println(a.toString() + " -> " + b));

        // NÚMERO DE FUNKOS POR MODELO
        Map<Model, Long> numeroPorModelo = funkos.stream().collect(Collectors.groupingBy( // Creamos un map donde agrupamos los funkos segun modelo y contamos el número de funkos de cada modelo
                Funko::getModel,
                Collectors.counting()));
        System.out.println("NUMERO DE FUNKOS POR MODELO: ");
        numeroPorModelo.forEach((a, b) -> System.out.println(a.toString() + "->" + b));

        // FUNKOS QUE HAN SIDO LANZADO EN EL 2023
        List<Funko> funkos2023  = funkos.stream().filter(f -> f.getReleaseData().getYear() == 2023).toList();   // Filtramos por funkos que el año de lanzamiento sea igual a 2023
        System.out.println("FUNKOS LANZADOS EN EL 2023: ");
        funkos2023.forEach(System.out::println);

        // NUMERO DE FUNKO DE STITCH Y LISTADO DE ELLOS
        List<Funko> funkosStitch = funkos.stream().filter(f -> f.getName().contains("Stitch")).toList(); // Filtramos por funkos que contengan Stitch en su nombre
        System.out.println("NUMERO DE FUNKOS DE STITCH: " + funkosStitch.size());      // Imprimimos el tamaño de la lista de Stitch
        System.out.println("LISTADO DE FUNKOS DE STITCH:");
        funkosStitch.forEach(System.out::println);

        // EXPORTAMOS LOS DATOS DE LA BASE DE DATOS A UN JSON LLAMADO "funkos.json"
        try {
            funkosService.export("funkos.json");
        } catch (RutaInvalidaException e) {
            throw new RuntimeException(e);
        }

        // EL PROGRAMA SIGUE CORRIENDO YA QUE EL CACHE TRABAJA PERMANENTEMENTE DE FORMA ASINCRONA
        // USAMOS EL SHUTDOWN DE LA CACHE PARA PARAR SU EJECUCION
        funkoCache.shutdown();
   }
}
