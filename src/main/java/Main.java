import java.io.InputStream; // ← ADD THIS LINE
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Driver;
import java.util.Enumeration;

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
                cleanupJdbcDrivers();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

      public static void cleanupJdbcDrivers() {
        // Deregister drivers loaded by the current classloader
        try {
            Enumeration<Driver> drivers = java.sql.DriverManager.getDrivers();
            ClassLoader cl = Main.class.getClassLoader();

            while (drivers.hasMoreElements()) {
                Driver d = drivers.nextElement();
                if (d.getClass().getClassLoader() == cl) {
                    try {
                        java.sql.DriverManager.deregisterDriver(d);
                    } catch (SQLException e) {
                        System.err.println("Error deregistering driver: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
        }

        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable t) {
        }
    }

    public static void main(String[] args) {
        // Ensure JDBC drivers/cleanup logic runs on JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanupJdbcDrivers));
        // Test the connection
        Connection conn = getConnection();

        if (conn != null) {
            System.out.println("Connection successful!");
            // Your database operations here
            // SELECT * FROM Item
            selectAllItems(conn);

            // SELECT * FROM Category
            selectAllCategories(conn);

            // SELECT * FROM Discount
            selectAllDiscounts(conn);

            
            // SELECT * FROM Customer
            selectAllCustomers(conn);

            // SELECT * FROM Orders
            selectAllOrders(conn);

            // SELECT * FROM Payment
            selectAllPayments(conn);

            // SELECT * FROM OrderLine
            selectAllOrderLines(conn);

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
        
        try (Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(query)) {

            while (res.next()) {
                System.out.println("Item: " + res.getString("Item") + 
                ", Category: " + res.getString("Category") + 
                ", Discount: " + res.getString("Discount"));
            }

        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public static void selectAllCategories(Connection conn) {
        String query = """
        SELECT ID, Name, Description 
        FROM Category 
        ORDER BY Name;
        """;
        System.out.println("\nCategories:");
        try (Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(query)) {
            while (res.next()) {
                System.out.println("ID: " + res.getInt("ID") +
                        ", Name: " + res.getString("Name") +
                        ", Description: " + res.getString("Description"));
            }
        } catch (SQLException e) {
            System.out.println("Error selecting categories: " + e.getMessage());
        }
    }

    public static void selectAllDiscounts(Connection conn) {
        String query = """
        SELECT ID, Description, discountType, startDate, endDate 
        FROM Discount 
        ORDER BY startDate;
        """;
        System.out.println("\nDiscounts:");
        try (Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(query)) {
            while (res.next()) {
                System.out.println("ID: " + res.getInt("ID") +
                        ", Description: " + res.getString("Description") +
                        ", Type: " + res.getString("discountType") +
                        ", Start: " + res.getDate("startDate") +
                        ", End: " + res.getDate("endDate"));
            }
        } catch (SQLException e) {
            System.out.println("Error selecting discounts: " + e.getMessage());
        }
    }

    public static void selectAllCustomers(Connection conn) {
        String query = """
        SELECT ID, FirstName, LastName, Phone, Email 
        FROM Customer 
        ORDER BY LastName, FirstName;
        """;
        System.out.println("\nCustomers:");
        try (Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(query)) {
            while (res.next()) {
                System.out.println("ID: " + res.getInt("ID") +
                        ", Name: " + res.getString("FirstName") + " " + res.getString("LastName") +
                        ", Phone: " + res.getString("Phone") +
                        ", Email: " + res.getString("Email"));
            }
        } catch (SQLException e) {
            System.out.println("Error selecting customers: " + e.getMessage());
        }
    }

    public static void selectAllOrders(Connection conn) {
        String query = """
        SELECT o.ID, o.CustomerID, c.FirstName, c.LastName 
        FROM Orders o 
        LEFT JOIN Customer c 
            ON o.CustomerID = c.ID 
        ORDER BY o.ID;
        """;
        System.out.println("\nOrders:");
        try (Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(query)) {
            while (res.next()) {
                System.out.println("Order ID: " + res.getInt("ID") +
                        ", CustomerID: " + res.getInt("CustomerID") +
                        ", Customer: " + res.getString("FirstName") + " " + res.getString("LastName"));
            }
        } catch (SQLException e) {
            System.out.println("Error selecting orders: " + e.getMessage());
        }
    }

    public static void selectAllPayments(Connection conn) {
        String query = """
        SELECT ID, orderID, paymentType, Amount, paymentDate 
        FROM Payment 
        ORDER BY paymentDate;
        """;
        System.out.println("\nPayments:");
        try (Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(query)) {
            while (res.next()) {
                System.out.println("ID: " + res.getInt("ID") +
                        ", OrderID: " + res.getInt("orderID") +
                        ", Type: " + res.getString("paymentType") +
                        ", Amount: " + res.getBigDecimal("Amount") +
                        ", Date: " + res.getDate("paymentDate"));
            }
        } catch (SQLException e) {
            System.out.println("Error selecting payments: " + e.getMessage());
        }
    }

    public static void selectAllOrderLines(Connection conn) {
        String query = """
        SELECT ol.OrderID, ol.ItemID, i.Name AS ItemName, ol.Quantity, ol.Total 
        FROM OrderLine ol 
        LEFT JOIN Item i 
            ON ol.ItemID = i.ID 
        ORDER BY ol.OrderID;
        """;
        System.out.println("\nOrderLines:");
        try (Statement stmt = conn.createStatement();
             ResultSet res = stmt.executeQuery(query)) {
            while (res.next()) {
                System.out.println("OrderID: " + res.getInt("OrderID") +
                        ", ItemID: " + res.getInt("ItemID") +
                        ", Item: " + res.getString("ItemName") +
                        ", Quantity: " + res.getInt("Quantity") +
                        ", Total: " + res.getBigDecimal("Total"));
            }
        } catch (SQLException e) {
            System.out.println("Error selecting order lines: " + e.getMessage());
        }
    }
}