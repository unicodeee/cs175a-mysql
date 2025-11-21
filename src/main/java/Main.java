import java.io.InputStream; // ← ADD THIS LINE
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.math.BigDecimal;
import java.sql.Driver;
import java.util.Enumeration;
import java.util.Scanner;

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
        String options = "";

        if (conn != null) {
            System.out.println("Connection successful!");

            // Prompt for options
            Scanner scanner = new Scanner(System.in);
            System.out.println("Select an option: ");
            System.out.println("1. View all items, categories, discounts, customers, orders, payments, and order lines");
            System.out.println("2. Insert a new item");
            System.out.println("3. Update an item");
            System.out.println("4. Delete an item");
            System.out.println("5. Search for items");
            System.out.println("6. Exit");
            options = scanner.nextLine();

            switch (options) {
                case "1":
                    promptSelectAll(conn, scanner);
                    break;
                case "2":
                    // Insert a new item
                    promptInsertItem(conn, scanner);
                    break;
                case "3":
                    // Update an item
                    System.out.println("Update not implemented yet.");
                    break;
                case "4":
                    // Delete an item
                    System.out.println("Delete not implemented yet.");
                    break;
                case "5":
                    // Search for items
                    System.out.println("Search not implemented yet.");
                    break;
                case "6":
                    // Exit
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
            
            scanner.close();
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

    public static void insertItem(Connection conn, String name, BigDecimal price, Integer categoryId, Integer discountId) {
        final String sql = "INSERT INTO Item (Name, Price, CategoryID, DiscountID) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setBigDecimal(2, price);

            if (categoryId != null) {
                ps.setInt(3, categoryId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            if (discountId != null) {
                ps.setInt(4, discountId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            int rows = ps.executeUpdate();
            System.out.println("Inserted item '" + name + "' — rows affected: " + rows);

        } catch (SQLException e) {
            System.err.println("Error inserting item: " + e.getMessage());
        }
    }

    public static void promptInsertItem(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter item name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter item price: ");
            String priceInput = scanner.nextLine().trim();
            BigDecimal price = new BigDecimal(priceInput);

            System.out.print("Enter category ID (optional, blank for null): ");
            String catInput = scanner.nextLine().trim();
            Integer categoryId = catInput.isEmpty() ? null : Integer.parseInt(catInput);

            System.out.print("Enter discount ID (optional, blank for null): ");
            String discInput = scanner.nextLine().trim();
            Integer discountId = discInput.isEmpty() ? null : Integer.parseInt(discInput);

            insertItem(conn, name, price, categoryId, discountId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered. Aborting insert.");
        }
    }

    public static void promptSelectAll(Connection conn, Scanner scanner) {
        System.out.println("Select one of the following to view all:");
        System.out.println("1. Items");
        System.out.println("2. Categories");
        System.out.println("3. Discounts");
        System.out.println("4. Customers");
        System.out.println("5. Orders");
        System.out.println("6. Payments");
        System.out.println("7. Order Lines");
        System.out.println("8. Exit");
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                selectAllItems(conn);
                break;
            case "2":
                selectAllCategories(conn);
                break;
            case "3":
                selectAllDiscounts(conn);
                break;
            case "4":
                selectAllCustomers(conn);
                break;
            case "5":
                selectAllOrders(conn);
                break;
            case "6":
                selectAllPayments(conn);
                break;
            case "7":
                selectAllOrderLines(conn);
                break;
            case "8":
                System.out.println("Goodbye!");
                System.exit(0);
            default:
                System.out.println("Invalid choice.");
        }
    }
}