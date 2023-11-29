import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // SQLite URL for a file-based database
    private static final String URL = "jdbc:sqlite:./database.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL);
    }

    // Create SQLite tables
    public static void createSQLiteTables() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Create SQLite table for englishquestions
            statement.execute("CREATE TABLE IF NOT EXISTS `englishquestions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`question` TEXT" +
                    ")");

            // Create SQLite table for clientquestions
            statement.execute("CREATE TABLE IF NOT EXISTS `clientquestions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`username` TEXT," +
                    "`question` TEXT," +
                    "`subject` TEXT" +
                    ")");

            // Create SQLite table for mathquestions
            statement.execute("CREATE TABLE IF NOT EXISTS `mathquestions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`question` TEXT" +
                    ")");

            // Create SQLite table for sciencequestions
            statement.execute("CREATE TABLE IF NOT EXISTS `sciencequestions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`question` TEXT" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
