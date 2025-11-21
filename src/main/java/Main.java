import java.io.InputStream; // ← ADD THIS LINE
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection != null) {
            return connection;
        }

        Properties props = new Properties();

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

            // SELECT * FROM Item

            // SELECT * FROM Category;
            // SELECT * FROM Item;
            // SELECT * FROM Discount;
            // SELECT * FROM Customer;
            // SELECT * FROM Orders;
            // SELECT * FROM Payment;
            // SELECT * FROM OrderLine;

            closeConnection();
        } else {
            System.out.println("Failed to establish connection.");
        }
    }

    public static void selectAllItems(Connection conn) {
        String query = """ 
            SELECT
                i.Name AS Item,
                c.Name AS Category,
                d.Description AS Discount
            FROM Item i
            LEFT JOIN Category c ON i.CategoryID = c.ID
            LEFT JOIN Discount d ON i.DiscountID = d.ID
            ORDER BY c.Name, i.Name;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            ResultSet res = stmt.executeQuery(query);

            while (res.next()) {
                System.out.println("Item: " + res.getString("Item") + 
                ", Category: " + res.getString("Category") + 
                ", Discount: " + res.getString("Discount"));
            }

        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }
}