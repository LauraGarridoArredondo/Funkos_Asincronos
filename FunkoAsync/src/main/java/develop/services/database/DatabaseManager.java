package develop.services.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * La clase DatabaseManager es un administrador de base de datos que gestiona la conexión y operaciones relacionadas con la base de datos.
 * Utiliza el patrón Singleton para proporcionar una única instancia de la clase.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final HikariDataSource dataSource;
    private boolean databaseInitTables;
    private String databaseUrl;
    private String databaseInitScript;
    private Connection conn;

    /**
     * Constructor privado para inicializar la base de datos y configurar la conexión.
     */
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

    /**
     * Obtiene la instancia única de DatabaseManager.
     *
     * @return La instancia única de DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }


    /**
     * Carga la configuración de la base de datos desde un archivo de propiedades.
     */
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

    /**
     * Inicializa las tablas de la base de datos ejecutando un script SQL.
     *
     * @param conn La conexión a la base de datos.
     */
    private synchronized void initTables(Connection conn) {
        try {
            executeScript(conn, databaseInitScript, true);
        } catch (FileNotFoundException e) {
            logger.error("Error al leer el fichero de inicialización de la base de datos " + e.getMessage());
        }
    }

    /**
     * Ejecuta un script SQL en la base de datos.
     *
     * @param conn           La conexión a la base de datos.
     * @param scriptSqlFile  El nombre del archivo de script SQL a ejecutar.
     * @param logWriter      Indica si se debe registrar la salida del script en la consola.
     * @throws FileNotFoundException Si el archivo de script SQL no se encuentra.
     */
    public synchronized void executeScript(Connection conn, String scriptSqlFile, boolean logWriter) throws FileNotFoundException {
        ScriptRunner sr = new ScriptRunner(conn);
        var file = ClassLoader.getSystemResource(scriptSqlFile).getFile();
        logger.debug("Ejecutando script de SQL " + file);
        Reader reader = new BufferedReader(new FileReader(file));
        sr.setLogWriter(logWriter ? new PrintWriter(System.out) : null);
        sr.runScript(reader);
    }

    /**
     * Obtiene una conexión a la base de datos.
     *
     * @return Una conexión a la base de datos.
     * @throws SQLException Si ocurre un error al obtener la conexión.
     */
    public synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}