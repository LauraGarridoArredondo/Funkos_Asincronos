package develop.repositories.funkos;

import develop.exceptions.funkos.FunkoNoAlmacenadoException;
import develop.exceptions.funkos.FunkoNoEncotradoException;
import develop.models.Funko;
import develop.models.IdGenerator;
import develop.models.Model;
import develop.services.database.DatabaseManager;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * La clase FunkosRepositoryImpl implementa la interfaz FunkosRepository y proporciona una implementación de los métodos para operaciones CRUD en objetos Funko.
 * Utiliza una base de datos y un generador de identificadores para gestionar los Funkos.
 */
public class FunkosRepositoryImpl implements FunkosRepository {
    private static FunkosRepositoryImpl instance;
    private final Logger logger = LoggerFactory.getLogger(FunkosRepositoryImpl.class);
    private final DatabaseManager db;
    private final IdGenerator idGenerator;

    /**
     * Constructor privado para crear una instancia de FunkosRepositoryImpl.
     *
     * @param db           El gestor de la base de datos para acceder a los datos de los Funkos.
     * @param idGenerator  El generador de identificadores para los Funkos.
     */
    private FunkosRepositoryImpl(DatabaseManager db, IdGenerator idGenerator) {
        this.db = db;
        this.idGenerator = idGenerator;
    }

    /**
     * Obtiene la instancia única de FunkosRepositoryImpl.
     *
     * @param db          El administrador de la base de datos utilizado para acceder al almacenamiento.
     * @param idGenerator El generador de identificadores utilizado para generar IDs únicos.
     * @return La instancia única de FunkosRepositoryImpl.
     */
    public synchronized static FunkosRepositoryImpl getInstance(DatabaseManager db, IdGenerator idGenerator) {
        if (instance == null) {
            instance = new FunkosRepositoryImpl(db, idGenerator);
        }
        return instance;
    }

    /**
     * Obtiene todos los Funkos del repositorio.
     *
     * @return Un CompletableFuture que representa la operación de búsqueda de todos los Funkos.
     */
    @Override
    public CompletableFuture<List<Funko>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<Funko> lista = new ArrayList<>();
            String query = "SELECT * FROM funkos";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Obteniendo todos los funkos");
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    Funko funko = Funko.builder()
                            .id(rs.getLong("ID"))
                            .COD(rs.getObject("cod", UUID.class))
                            .myId(rs.getLong("MyId"))
                            .name(rs.getString("nombre"))
                            .model(Model.valueOf(rs.getString("modelo")))
                            .price(rs.getDouble("precio"))
                            .releaseData(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .createdAt(rs.getObject("created_at", LocalDateTime.class))
                            .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
                            .build();
                    lista.add(funko);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar todos los funkos", e);
                throw new CompletionException(e);
            }
            return lista;
        });
    }


    /**
     * Busca y devuelve Funkos por nombre en el repositorio.
     *
     * @param nombre El nombre por el que se realizará la búsqueda.
     * @return Un CompletableFuture que representa la operación de búsqueda de Funkos por nombre.
     */
    @Override
    public CompletableFuture<List<Funko>> findByNombre(String nombre) {
        return CompletableFuture.supplyAsync(() -> {
            var lista = new ArrayList<Funko>();
            String query = "SELECT * FROM funkos WHERE nombre LIKE ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Obteniendo todos los funkos por nombre que contenga: " + nombre);
                stmt.setString(1, "%" + nombre + "%");
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    Funko funko = Funko.builder()
                            .id(rs.getLong("ID"))
                            .COD(rs.getObject("cod", UUID.class))
                            .myId(rs.getLong("MyId"))
                            .name(rs.getString("nombre"))
                            .model(Model.valueOf(rs.getString("modelo")))
                            .price(rs.getDouble("precio"))
                            .releaseData(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .createdAt(rs.getObject("created_at", LocalDateTime.class))
                            .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
                            .build();
                    lista.add(funko);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar funkos por nombre", e);
                throw new CompletionException(e);
            }
            return lista;
        });
    }

    /**
     * Busca un Funko por su ID en el repositorio.
     *
     * @param id El ID del Funko que se busca.
     * @return Un CompletableFuture que representa la operación de búsqueda de Funko por ID.
     */
    @Override
    public CompletableFuture<Optional<Funko>> findById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Funko> funko = Optional.empty();
            String query = "SELECT * FROM funkos WHERE ID =?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                stmt.setLong(1, id);
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    funko = Optional.of(Funko.builder()
                            .id(rs.getLong("ID"))
                            .COD(rs.getObject("cod", UUID.class))
                            .myId(rs.getLong("MyId"))
                            .name(rs.getString("nombre"))
                            .model(Model.valueOf(rs.getString("modelo")))
                            .price(rs.getDouble("precio"))
                            .releaseData(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .createdAt(rs.getObject("created_at", LocalDateTime.class))
                            .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
                            .build()
                    );
                }
            } catch (SQLException e) {
                logger.error("Error al buscar funko por id", e);
                throw new CompletionException(e);
            }
            return funko;
        });
    }

    /**
     * Guarda un Funko en el repositorio.
     *
     * @param funko El Funko que se va a guardar.
     * @return Un CompletableFuture que representa la operación de guardado.
     */
    @Override
    public CompletableFuture<Funko> save(Funko funko) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "INSERT INTO funkos (cod, MyId, nombre, modelo, precio, fecha_lanzamiento, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
            ) {
                logger.debug("Guardando el funko: " + funko);

                funko.setMyId(idGenerator.getIdAndIncrement());
                funko.setUpdatedAt(LocalDateTime.now());
                stmt.setObject(1, funko.getCOD());
                stmt.setLong(2, funko.getMyId());
                stmt.setString(3, funko.getName());
                stmt.setString(4, funko.getModel().toString());
                stmt.setDouble(5, funko.getPrice());
                stmt.setObject(6, funko.getReleaseData());
                stmt.setObject(7, funko.getCreatedAt());
                stmt.setObject(8, funko.getUpdatedAt());
                var res = stmt.executeUpdate();
                if (res > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    while (rs.next()) {
                        funko.setId(rs.getLong(1));
                    }
                    rs.close();
                } else {
                    logger.error("Funko no guardado con id: " + funko.getId());
                    throw new FunkoNoAlmacenadoException("Funko no guardado con id: " + funko.getId());
                }
            } catch (SQLException | FunkoNoAlmacenadoException e) {
                logger.error("Error al guardar el funko", e);
                throw new CompletionException(e);
            }
            return funko;
        });
    }


    /**
     * Actualiza un Funko en el repositorio.
     *
     * @param funko El Funko que se va a actualizar.
     * @return Un CompletableFuture que representa la operación de actualización.
     */
    @Override
        public CompletableFuture<Funko> update(Funko funko) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "UPDATE funkos SET nombre = ?, modelo = ?, precio = ?, updated_at = ? WHERE ID = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Actualizando el funko: " + funko);
                funko.setUpdatedAt(LocalDateTime.now());
                stmt.setString(1, funko.getName());
                stmt.setString(2, funko.getModel().toString());
                stmt.setDouble(3, funko.getPrice());
                stmt.setObject(4, funko.getUpdatedAt());
                stmt.setLong(5, funko.getId());
                var res = stmt.executeUpdate();
                if (res > 0) {
                    logger.debug("Funko actualizado");
                } else {
                    logger.error("Funko no actualizado al no encontrarse en la base de datos con id: " + funko.getId());
                    throw new FunkoNoEncotradoException("Funko no encontrado con id: " + funko.getId());
                }
            } catch (SQLException | FunkoNoEncotradoException e) {
                throw new CompletionException(e);
            }
            return funko;
        });
    }

    /**
     * Borra un Funko por su ID en el repositorio.
     *
     * @param aLong El ID del Funko que se va a borrar.
     * @return Un CompletableFuture que representa la operación de borrado.
     */
    @Override
    public CompletableFuture<Boolean> deleteById(Long aLong) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "DELETE FROM funkos WHERE ID = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Borrando el funko con id: " + aLong);
                stmt.setLong(1, aLong);
                var res = stmt.executeUpdate();
                stmt.close();
                return res > 0;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Borra todos los Funkos del repositorio.
     *
     * @return Un CompletableFuture que representa la operación de borrado de todos los Funkos.
     */
    @Override
    public CompletableFuture<Void> deleteAll() {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM funkos";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

}