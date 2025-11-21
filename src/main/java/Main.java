import java.io.InputStream;  // ← ADD THIS LINE
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    
    private static Connection connection = null;
    
    public static Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        
        Properties props = new Properties();
        
        // Remove "/resources/" - just use "app.properties"
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("app.properties")) {
            
            if (is == null) {
                System.err.println("Could not find app.properties in resources!");
                return null;
            }
            
            props.load(is);
            
            // Get database properties
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            
            // Load MySQL JDBC driver
            Class.forName(driver);
            
            // Establish connection
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully!");
            
        } catch (IOException e) {
            System.err.println("Error reading app.properties file: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        // Test the connection
        Connection conn = getConnection();
        
        if (conn != null) {
            System.out.println("Connection successful!");
            // Your database operations here
            
            closeConnection();
        } else {
            System.out.println("Failed to establish connection.");
        }
    }
}