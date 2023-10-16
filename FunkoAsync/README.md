# Proyecto Java con H2
***
Este proyecto es una aplicación en Java que hace uso de los beneficios de la programación asincrona para llevar a cabo acciones bloqueantes en distintos hilos y asi evitar el bloqueo del hilo principal, para llevar a cabo este programa nos apoyamos en la base de datos H2 donde guardaremos todos los datos relacionada con el objeto Funko quien será el protagonista en todo el planteamiento. Además de configurar y utilizar la base de datos en H2, crearemos un servicio de cache, un servicio de almacenamiento y un servicio repositorio quienes serán inyectados al servicio principal de Funkos y desde el cual haremos todas las operaciones en el main. A continuacion, Iremos paso a paso viendo los puntos claves para desarrollar este proyecto (¡Espero que os guste el proyecto!):

## Requisitos
***
* Java 8 o superior
* Gradle

## Configuración
***
### Paso 1: Dependencias de Gradle

Agrega las siguientes dependencias a tu archivo `build.gradle`:

```kotlin
dependencies {
testImplementation(platform("org.junit:junit-bom:5.9.1"))
testImplementation("org.junit.jupiter:junit-jupiter")
// Lombok
implementation("org.projectlombok:lombok:1.18.30")
annotationProcessor ("org.projectlombok:lombok:1.18.30")
// Logger
implementation("ch.qos.logback:logback-classic:1.4.11")
// Project Reactor
implementation("io.projectreactor:reactor-core:3.5.10")
// H2
implementation("com.h2database:h2:2.2.224")
// HikaryCP para la conexion con la base de datos
implementation("com.zaxxer:HikariCP:5.0.1")
// Ibatis
implementation("org.mybatis:mybatis:3.5.13")
// Gson
implementation ("com.google.code.gson:gson:2.8.8")
// Mockito
testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
testImplementation("org.mockito:mockito-core:5.5.0")
}

```

### Paso 2: Crear un enum Model

Crear un enum `Model` donde definiremos los tipos de modelos o categorias de los Funko, los cuales pueden ser: `MARVEL`, `DISNEY`, `ANIME` u `OTROS`.

```java
public enum Model {
    MARVEL, DISNEY, ANIME, OTROS;
}
```

### Paso 3: Crear la clase Funko

Crear la clase `Funko` donde definirimos los atributos y metodo de un objeto funko, estos atributos son: `id`, `COD`, `myId`, `name`, `model`, `price`, `releaseData`, `createdAt` y `updateAt`. Y entre otros metodos tendremos el `constructor` y el `toString()`.

```java
@Data
@Builder
public class Funko {
private long id; 
private UUID COD; 
private long myId; 
private String name; 
private Model model; 
private double price; 
private LocalDate releaseData; 
@Builder.Default
private LocalDateTime createdAt = LocalDateTime.now(); 
@Builder.Default
private LocalDateTime updatedAt = LocalDateTime.now();
}
```
### Paso 4: Crear la clase IdGenerador

Crear la clase `IdGenerador`, esta clase servira como monitor para asignar el atributor `myId` a los objetos Funkos que sean guardado en una futura base de datos, ya que hablamos de programacion asincrona, tendremos que apoyarnos en esta clase singleton con Lock para asignar dicho valor sin crear conflicto en la ejecucion simultanea de hilos. La clase tendra 3 atributos los cuales son `instance` que representa la instancia unica de la clase, `id` que lleva el control de la id de los Funkos y un `locker` que permitira que asignar el id sin conflicto. Su metodo más destacable es el de `getIdAndIncrement()` quien se encarga de sumar y asignar la `id` a los funkos.

```java
public class IdGenerator {
    private static IdGenerator instance;
    private static long id = 0;
    private final Lock locker = new ReentrantLock(true);
    
    // Devuelve la instancia unica de la clase
    public static synchronized IdGenerator getInstance(){
        if(instance == null){
           instance = new IdGenerator();
        }
        return instance;
    }
    
    // Incrementa y devuelve el id
    public Long getIdAndIncrement() {
        Long idCopia = 0L;
        locker.lock();
        id++;
        idCopia = id;
        locker.unlock();
        return idCopia;
    }
    
    // Resetea el conteo de id a 0 (usaremos en los test)
    public void resetId() {
        this.id = 0L;
    }
}
```

### Paso 5: Creamos los adaptadores para nuestro JSON

Creamos 3 clases adaptadores `LocalDateAdapter`, `LocalDateTimeAdapter` y `UuidAdapter` que servirán para leer y escribir de JSON datos de tipo LocalDate, LocalDateTime y UUID respectivamente,  

```java
public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    
    @Override
    public LocalDate read(final JsonReader jsonReader) throws IOException {
        return LocalDate.parse(jsonReader.nextString());
    }
    
    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
        jsonWriter.value(localDate.toString());

    }
}
```

```java
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        return LocalDateTime.parse(jsonReader.nextString());
    }
    
    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        jsonWriter.value(localDateTime.toString());

    }
}
```
```java
public class UuidAdapter extends TypeAdapter<UUID> {
    
    @Override
    public UUID read(final JsonReader jsonReader) throws IOException {
        return UUID.fromString(jsonReader.nextString());
    }
    
    @Override
    public void write(JsonWriter jsonWriter, UUID uuid) throws IOException {
        jsonWriter.value(uuid.toString());

    }
}
```

### Paso 6: Creamos la clase MyLocale

Creamos la clase `MyLocale` que usaremos para formatear la salida del `price` y de `releaseData` por consola a formato local de España con sus metodos `toLocalMoney` y `toLocalDate`.

```java
public class MyLocale {
    private static final Locale locale = new Locale("es","ES");
    
    public static String toLocalDate(LocalDate date) {
        return date.format(
                DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        );
    }
    
    public static String toLocalMoney(double money) {
        return NumberFormat.getCurrencyInstance(locale).format(money);
    }

}
```

### Paso 7: Creamos las excepciones personalizadas para el repositorio

Crearemos primeramente la clase abstracta `FunkoException` que heredara de `Exception` y luego crearemos una clase por cada excepcion personalizada, todas ellas heredaran a su vez de `FunkoException`. Estas clases por excepcion personalizada serán las clases: `FunkoNoAlmacenadoException` (que se lanzará cuando haya un error al intentar almacenar en Funko en la base de datos) y `FunkoNoEncontradoException` (que se lanzará cuando no se consiga el Funko en la base de datos).

```java
public abstract class FunkoException extends Exception {
    
    public FunkoException(String message) {
        super(message);
    }
}
```

```java
public class FunkoNoAlmacenadoException extends FunkoException {
    
    public FunkoNoAlmacenadoException(String message) {
        super(message);
    }
}
```

```java
public class FunkoNoEncotradoException extends FunkoException {
    
    public FunkoNoEncotradoException(String message) {
        super(message);
    }
}
```

### Paso 8: Creamos las excepciones personalizadas para el servicio de almacenamiento

Crearemos primeramente la clase abstracta `StorageException` que heredara de `Exception` y luego crearemos una clase por cada excepcion personalizada, todas ellas heredaran a su vez de `FunkoException`. En este caso, solo crearemos una excepcion personalizada llamada `RutaInvalidaException` que se lanzara cuando la ruta pasada sea invalida, esta clase heredará de `StorageException`.

```java
public abstract class StorageException extends Exception  {
    
    public StorageException(String message) {
        super(message);
    }
}
```

```java
public class RutaInvalidaException extends StorageException {
    
    public RutaInvalidaException(String message) {
        super(message);
    }
}
```

### Paso 9: Creamos la clase DatabaseManager

Crea la clase `DatabaseManager` que maneja la conexión a la base de datos H2 de forma asincrona con el `Hikary`. Esta clase es un singleton y tiene un método `getInstance` para obtener la instancia. También tiene un método `executeScript()` que ejecuta un script SQL desde un archivo y un `initTable()` para iniciar las tablas.

```java
public class DatabaseManager {
    private static DatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final HikariDataSource dataSource;
    private boolean databaseInitTables;
    private String databaseUrl;
    private String databaseInitScript;
    private Connection conn;
    
    private DatabaseManager() {
        loadProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection()) {
            if (databaseInitTables) {
                initTables(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private synchronized void loadProperties() {
        logger.debug("Cargando fichero de configuración de la base de datos");
        try {
            var file = ClassLoader.getSystemResource("database.properties").getFile();
            var props = new Properties();
            props.load(new FileReader(file));
            databaseUrl = props.getProperty("database.url", "jdbc:h2:./Funkos");
            databaseInitTables = Boolean.parseBoolean(props.getProperty("database.initTables", "false"));
            databaseInitScript = props.getProperty("database.initScript", "init.sql");
        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuración de la base de datos " + e.getMessage());
        }
    }
    
    private synchronized void initTables(Connection conn) {
        try {
            executeScript(conn, databaseInitScript, true);
        } catch (FileNotFoundException e) {
            logger.error("Error al leer el fichero de inicialización de la base de datos " + e.getMessage());
        }
    }
    
    public synchronized void executeScript(Connection conn, String scriptSqlFile, boolean logWriter) throws FileNotFoundException {
        ScriptRunner sr = new ScriptRunner(conn);
        var file = ClassLoader.getSystemResource(scriptSqlFile).getFile();
        logger.debug("Ejecutando script de SQL " + file);
        Reader reader = new BufferedReader(new FileReader(file));
        sr.setLogWriter(logWriter ? new PrintWriter(System.out) : null);
        sr.runScript(reader);
    }
    
    public synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

### Paso 10: Creamos la clase Repository y su implementacion

Primeramente crearemos la interface generica CRUD la cual establecerá todos los metodos asincronos para interactuar con la base de datos, esta interface tendran que ser implementado en el repository de Funko, entre esos metodos tendremos los principales de un CRUD como son: `save()`, `update()`, `findById()`, `findAll()`, `deleteById()` y `deleteAll()`.

```java
public interface CrudRepository<T, ID> {
    
    CompletableFuture<Funko> save(T t) throws SQLException, FunkoNoAlmacenadoException;
    
    CompletableFuture<Funko> update(T t) throws SQLException, FunkoNoEncotradoException;
    
    CompletableFuture<Optional<Funko>> findById(ID id) throws SQLException, FunkoNoEncotradoException;
    
    CompletableFuture<List<Funko>> findAll() throws SQLException;
    
    CompletableFuture<Boolean> deleteById(ID id) throws SQLException, FunkoNoEncotradoException;
    
    CompletableFuture<Void> deleteAll() throws SQLException;
```

Seguidamente creamos la interface `FunkosRepository` la cual extenderá de `CrudRepository` y definira los tipos de valores T y ID como <Funko, Longo> y creará un nuevo metodo `findByNombre()` asincrono.

```java
public interface FunkosRepository extends CrudRepository<Funko, Long> {
    
    CompletableFuture<List<Funko>> findByNombre(String nombre) throws SQLException, FunkoNoEncotradoException;
}
```

Por ultimo, crearemos la clase `FunkosRepositoryImpl` que implementara la interfaz `FunkosRepository` y sus metodos de acuerdo a sus necesidades y será tambien donde se realice la conexion de las consultas SQL con la base de datos, por ello, la clase tendra ese sus atributos una instancia del `DatabaseManager` que se le inyectara en el constructor asi como tambien una instancia de `IdGenerator` que le asignara el `myId` a los funkos segun entren en la base de datos. Cabe destacar que al ser asincrono se configura como un singleton con proteccion a entornos multihilos.

```java
public class FunkosRepositoryImpl implements FunkosRepository {
    private static FunkosRepositoryImpl instance;
    private final Logger logger = LoggerFactory.getLogger(FunkosRepositoryImpl.class);
    private final DatabaseManager db;
    private final IdGenerator idGenerator;
    
    private FunkosRepositoryImpl(DatabaseManager db, IdGenerator idGenerator) {
        this.db = db;
        this.idGenerator = idGenerator;
    }
    
    public synchronized static FunkosRepositoryImpl getInstance(DatabaseManager db, IdGenerator idGenerator) {
        if (instance == null) {
            instance = new FunkosRepositoryImpl(db, idGenerator);
        }
        return instance;
    }

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
```

### Paso 11: Creamos el servicio de almacenamiento Storage y su implementacion

Para crear el servicio de almacenamiento crearemos primero la interface generica Storage la cual establecerá todos los metodos asincronos para manejar el almacenamiento de archivos, esta interface tendran que ser implementado en el almacenamiento de Funko y contara con 2 metodos: `importCsv()` y `ExportJson()` para importar Csv y exportar Json.


```java
public interface Storage<T> {

    CompletableFuture<Void> exportJson(List<T> data, String file) throws IOException, RutaInvalidaException;

    CompletableFuture <List<T>> importCsv() throws IOException;
}
```

Seguidamente creamos la interface `FunkoStorage` la cual extendera de la interface `Storage` y establecerá el tipo de dato que vamos a usar en el servicio de almacenamiento, en este caso `Funko`. 

```java
public interface FunkoStorage extends Storage<Funko> { }
```

Por ultimo, creamos la clase `FunkoStorageImpl` que implementará la interface `FunkoStorage` y sus metodos. Adaptandolo a sus necesidades. Estos metodos asincronos como ya mencionamos antes serán  `importCsv()` y `exportJson`.

```java
public class FunkoStorageImpl implements FunkoStorage {
    private final Logger logger = LoggerFactory.getLogger(FunkoStorageImpl.class);
    private static FunkoStorageImpl instance;

    private FunkoStorageImpl() {}
    
    public static synchronized FunkoStorageImpl getInstance() {
        if (instance == null) {
            instance = new FunkoStorageImpl();
        }
        return instance;
    }
    
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
```

### Paso 12: Creamos el servicio de Cache y su implementacion

Para crear el servicio de cache primeramente crearemos la interface generica `Cache` la cual establecerá todos los metodos asincronos para manejar el cache del servicio, esta interface tendran que ser implementado en el cache de Funko y contara con los siguientes metodos asincronos: `put()`, `get()`, `remove`, `clear` y `shutdown` los cuales se encargan de agregar datos al cache, recuperar datos, remover datos, limpiar el cache y lo apagar el cache.
```java
public interface Cache<K, V> {
    
    CompletableFuture <Void> put(K key, V value);
    
    CompletableFuture  <Optional<V>> get(K key);
    
    CompletableFuture <Void> remove(K key);
    
    CompletableFuture <Void> clear();
    
    CompletableFuture <Void> shutdown();
}
```

Seguidamente creamos la interface `FunkoCache` la cual extendera de la interface `Cache` y establecerá el tipo de dato que vamos a usar en el servicio de cache, en este caso `Funko`. 

```java
public interface FunkoCache extends Cache<Long, Funko> { }
```

Por ultimo, creamos la clase `FunkoCacheImpl` que implementará la interface `FunkoCache` y sus metodos. Adaptandolo a sus necesidades.

```java
public class FunkoCacheImpl implements  FunkoCache {
    private final Logger logger = LoggerFactory.getLogger(FunkoCacheImpl.class);
    private final int maxSize;
    private final Map<Long, Funko> cache;
    private final ScheduledExecutorService cleaner;
    private static FunkoCacheImpl instance;
    
    public static synchronized FunkoCacheImpl getInstance(int maxSize) {
        if(instance == null){
            instance = new FunkoCacheImpl(maxSize);
        }
        return instance;
    }

    public int getTamano(){
        return cache.size();
    }

    private FunkoCacheImpl(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<Long, Funko>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Funko> eldest) {
                return size() > maxSize;
            }
        };
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear, 2, 2, TimeUnit.MINUTES);
    }
    
    @Override
    public CompletableFuture<Void> put(Long key, Funko value) {
        return CompletableFuture.runAsync(() -> {
            logger.debug("Añadiendo funko a cache con id: " + key + " y valor: " + value);
            cache.put(key, value);
        });
    }
    
    @Override
    public CompletableFuture<Optional<Funko>> get(Long key) {
        return CompletableFuture.supplyAsync(() -> {
           logger.debug("Obteniendo funko de cache con id: " + key);
           Optional<Funko> funko = Optional.ofNullable(cache.get(key));
           if(funko.isPresent() && funko!= null) {
               return funko;
           } else {
               return Optional.empty();
           }
        });
    }
    
    @Override
    public CompletableFuture<Void> remove(Long key) {
        return CompletableFuture.runAsync(() -> {
           logger.debug("Eliminando funko de cache con id: " + key);
           cache.remove(key);
        });
    }
    
    @Override
    public CompletableFuture<Void> clear() {
        return CompletableFuture.runAsync(() -> {
            cache.entrySet().removeIf(entry -> {
               boolean shouldRemove = entry.getValue().getUpdatedAt().plusMinutes(2).isBefore(LocalDateTime.now());
               if (shouldRemove) {
                   logger.debug("Autoeliminando por caducidad funko de cache con id: " + entry.getKey());
               }
               return shouldRemove;
            });
        });
    }
    
    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
           cleaner.shutdown();
        });
    }
}
```

### Paso 14: Creamos el Servicio Funko y su implementacion

Crearemos la interface `FunkosServices` donde estableceremos todos los metodos asincronos que más adelante deberan ser implementados en el `FunkosServiceImpl`, esta interfaz tendra los siguientes metodos: `findAll()`, `findAllByNombre()`, `findById()`, `save()`, `update()`, `deleteById()` y `deleteAll()`. 

```java
public interface FunkosService {
    
    CompletableFuture <List<Funko>> findAll() throws SQLException, ExecutionException, InterruptedException;
    
    CompletableFuture <List<Funko>> findAllByNombre(String nombre) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException;
    
    CompletableFuture <Optional<Funko>> findById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException;
    
    CompletableFuture <Funko> save(Funko alumno) throws SQLException, ExecutionException, InterruptedException, FunkoNoAlmacenadoException;
    
    CompletableFuture <Funko> update(Funko alumno) throws SQLException, FunkoNoEncotradoException, ExecutionException, InterruptedException;
    
    CompletableFuture <Boolean> deleteById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException;
    
    CompletableFuture <Void> deleteAll() throws SQLException, ExecutionException, InterruptedException;
}
```

Por ultimo, creamos la clase principal de todo el programa llamada `FunkosServiceImpl`, esta sera un singleton protegida por hilos e implementara la interface `FunkoService` y sus metodos haciendo usos de los metodos codificados previamente en los servicios de almacenamiento, cache y repositorio, para ellos, la clase `FunkosServiceImpl` recibira una instancia de cada uno de los servicios por inyeccion en el constructor y los almacenara en sus atributos teniendo asi los siguientes atributos para cada instancia: `cache`, `funkosRepository` y `funkoStorage` donde se guardan las instancias de los servicios respectivamente. Adaptando tambien los metodos implementados segun requerimientos.

```java
public class FunkosServiceImpl implements FunkosService {
    private static FunkosServiceImpl instance;
    private final FunkoCache cache;
    private final Logger logger = LoggerFactory.getLogger(FunkosServiceImpl.class);
    private final FunkosRepository funkosRepository;
    private final FunkoStorage funkoStorage;
    
    private FunkosServiceImpl(FunkosRepository funkosRepository, FunkoCache funkoCache, FunkoStorage funkoStorage) {
        this.funkosRepository = funkosRepository;
        this.cache = funkoCache;
        this.funkoStorage = funkoStorage;
    }
    
    public static synchronized FunkosServiceImpl getInstance(FunkosRepository funkosRepository, FunkoCache funkoCache, FunkoStorage funkoStorage) {
        if (instance == null) {
            instance = new FunkosServiceImpl(funkosRepository, funkoCache, funkoStorage);
        }
        return instance;
    }
    
    @Override
    public CompletableFuture <List<Funko>> findAll() throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Obteniendo todos los funkos");
        return funkosRepository.findAll();
    }
    
    @Override
    public CompletableFuture <List<Funko>> findAllByNombre(String nombre) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
           logger.debug("Obteniendo todos los funkos con nombre: " + nombre);
           return funkosRepository.findByNombre(nombre);
    }
    
    @Override
    public CompletableFuture <Optional<Funko>> findById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
            logger.debug("Obteniendo el funko con id: " + id);
            CompletableFuture<Optional<Funko>> funko = cache.get(id);
            if(funko.get().isPresent()) {
                logger.debug("Funko encontrado en cache");
                return funko;
            } else {
                logger.debug("Funko no encontrado en cache\nBuscando en la base de datos");
                return funkosRepository.findById(id);
            }
    }
    
    @Override
    public CompletableFuture <Funko> save(Funko funko) throws SQLException, ExecutionException, InterruptedException, FunkoNoAlmacenadoException {
            logger.debug("Guardando funko "+ funko);
            CompletableFuture<Funko> funkoGuardado = funkosRepository.save(funko);
            cache.put(funkoGuardado.get().getId(), funkoGuardado.get());
            return funkoGuardado;
    }
    
    @Override
    public CompletableFuture<Funko> update(Funko funko) throws SQLException, FunkoNoEncotradoException, ExecutionException, InterruptedException {
            logger.debug("Actualizando funko: " + funko);
            cache.put(funko.getId(), funko);
            return funkosRepository.update(funko);
    }
    
    @Override
    public CompletableFuture<Boolean> deleteById(long id) throws SQLException, ExecutionException, InterruptedException, FunkoNoEncotradoException {
            logger.debug("Borrando funko con id: " + id);
            CompletableFuture <Boolean> deleted = funkosRepository.deleteById(id);
            if(deleted.get()) {
                cache.remove(id);
            }
            return deleted;
    }
    
    @Override
    public CompletableFuture <Void> deleteAll() throws SQLException, ExecutionException, InterruptedException {
            logger.debug("Borrando todos los funkos");
            cache.clear();
            return funkosRepository.deleteAll();
    }
    
    public CompletableFuture <Void> export(String file) throws SQLException, IOException, ExecutionException, InterruptedException, RutaInvalidaException {
            return CompletableFuture.runAsync(() -> {
                logger.debug("Guardando funkos en archivo");
                try {
                    funkoStorage.exportJson(this.findAll().get(), file);
                } catch (IOException | SQLException | ExecutionException | InterruptedException | RutaInvalidaException e) {
                    logger.error("Error al guardar los funkos en archivo JSON");
                    throw new RuntimeException(e);
                }
            });
    }
    
    public CompletableFuture <List<Funko>> importFile() throws SQLException, IOException {
            logger.debug("Importando funkos de archivo CSV");
            return funkoStorage.importCsv();
    }
}
```
## Ejecucion

Una vez codificado todo el programa y de haber testeado los metodos, escribiremos en el main:

```java
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
```