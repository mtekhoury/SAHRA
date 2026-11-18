package sahra.db;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens connections to the MySQL database.
 * The URL, user name and password are read from config.properties
 * (in the project folder), so the password is never written in the code.
 */
public final class Database {

    private static Properties settings;

    private Database() { }

    private static Properties settings() throws SQLException {
        if (settings == null) {
            Path file = Paths.get("config.properties");
            if (!Files.exists(file)) {
                throw new SQLException("config.properties was not found in " + file.toAbsolutePath()
                        + ".\nCopy config.example.properties, rename it to config.properties, "
                        + "and put your MySQL password in it.");
            }
            Properties p = new Properties();
            try (Reader reader = Files.newBufferedReader(file)) {
                p.load(reader);
            } catch (IOException e) {
                throw new SQLException("Could not read config.properties: " + e.getMessage());
            }
            settings = p;
        }
        return settings;
    }

    /** Returns a new connection. Always use it inside try-with-resources so it gets closed. */
    public static Connection connect() throws SQLException {
        Properties p = settings();
        return DriverManager.getConnection(
                p.getProperty("db.url", "jdbc:mysql://localhost:3306/sahra"),
                p.getProperty("db.user", "root"),
                p.getProperty("db.password", ""));
    }
}
