import java.io.InputStream;
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
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
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
        
        Connection conn = getConnection();
        if (conn == null) {
            System.out.println("Failed to connect to database.");
            return;
        }
        System.out.println("Connection successful!");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                System.out.println("\n=== Supermarket Management System ===");
                System.out.println("1. View Data (Tables & Views)");
                System.out.println("2. Manage Items (Insert, Update, Delete)");
                System.out.println("3. Place Order (Transactional Workflow)");
                System.out.println("4. Check Price (Stored Function)");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> handleViewMenu(conn, scanner);
                    case "2" -> handleItemMenu(conn, scanner);
                    case "3" -> placeOrderTransaction(conn, scanner);
                    case "4" -> checkItemPrice(conn, scanner);
                    case "5" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            }
        }
        
        closeConnection();
    }

    public static void selectAllItems(Connection conn) {
        String query = """ 
            SELECT
                i.ID,
                i.Name AS Item,
                c.Name AS Category,
                d.Description AS Discount,
                i.Price
            FROM Item i
            LEFT JOIN Category c ON i.CategoryID = c.ID
            LEFT JOIN Discount d ON i.DiscountID = d.ID
            ORDER BY c.Name, i.Name;
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet res = stmt.executeQuery()) {

            System.out.println("\n--- Items ---");
            while (res.next()) {
                System.out.printf("ID: %d | Item: %s | Price: %s | Category: %s | Discount: %s%n",
                    res.getInt("ID"),
                    res.getString("Item"),
                    res.getBigDecimal("Price"),
                    res.getString("Category"),
                    res.getString("Discount"));
            }

        } catch (SQLException e) {
            System.out.println("Error selecting items: " + e.getMessage());
        }
    }

    public static void selectAllCategories(Connection conn) {
        String query = "SELECT ID, Name, Description FROM Category ORDER BY Name";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet res = stmt.executeQuery()) {
            System.out.println("\n--- Categories ---");
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
        String query = "SELECT ID, Description, discountType, startDate, endDate FROM Discount ORDER BY startDate";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet res = stmt.executeQuery()) {
            System.out.println("\n--- Discounts ---");
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
        String query = "SELECT ID, FirstName, LastName, Phone, Email FROM Customer ORDER BY LastName, FirstName";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet res = stmt.executeQuery()) {
            System.out.println("\n--- Customers ---");
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
        LEFT JOIN Customer c ON o.CustomerID = c.ID 
        ORDER BY o.ID
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet res = stmt.executeQuery()) {
            System.out.println("\n--- Orders ---");
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
        String query = "SELECT ID, orderID, paymentType, Amount, paymentDate FROM Payment ORDER BY paymentDate";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet res = stmt.executeQuery()) {
            System.out.println("\n--- Payments ---");
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
        LEFT JOIN Item i ON ol.ItemID = i.ID 
        ORDER BY ol.OrderID
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet res = stmt.executeQuery()) {
            System.out.println("\n--- OrderLines ---");
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
        String sql = """
        INSERT INTO Item (Name, Price, CategoryID, DiscountID) 
        VALUES (?, ?, ?, ?);
        """;
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
            Integer categoryId = catInput.isEmpty() ? null : Integer.valueOf(catInput);

            System.out.print("Enter discount ID (optional, blank for null): ");
            String discInput = scanner.nextLine().trim();
            Integer discountId = discInput.isEmpty() ? null : Integer.valueOf(discInput);

            insertItem(conn, name, price, categoryId, discountId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered. Aborting insert.");
        }
    }

    public static void handleItemMenu(Connection conn, Scanner scanner) {
        System.out.println("\n--- Manage Items ---");
        System.out.println("1. Insert Item");
        System.out.println("2. Update Item");
        System.out.println("3. Delete Item");
        System.out.println("4. Back");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> promptInsertItem(conn, scanner);
            case "2" -> updateItem(conn, scanner);
            case "3" -> deleteItem(conn, scanner);
            case "4" -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    public static void updateItem(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter Item ID to update: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter new Name: ");
            String name = scanner.nextLine().trim();
            
            System.out.print("Enter new Price: ");
            BigDecimal price = new BigDecimal(scanner.nextLine().trim());

            String sql = """
            UPDATE Item 
            SET Name = ?, Price = ? 
            WHERE ID = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setBigDecimal(2, price);
                ps.setInt(3, id);
                
                int rows = ps.executeUpdate();
                if (rows > 0) System.out.println("Item updated successfully.");
                else System.out.println("Item not found.");
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error updating item: " + e.getMessage());
        }
    }

    public static void deleteItem(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter Item ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            String sql = "DELETE FROM Item WHERE ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) System.out.println("Item deleted successfully.");
                else System.out.println("Item not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting item (check constraints): " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        }
    }

    public static void placeOrderTransaction(Connection conn, Scanner scanner) {
        System.out.println("\n--- Place Order (Transaction) ---");
        try {
            System.out.print("Enter Customer ID: ");
            int customerId = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter Item ID: ");
            int itemId = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            conn.setAutoCommit(false); // Start Transaction

            try {
                // 1. Create Order
                String orderSql = "INSERT INTO Orders (CustomerID) VALUES (?)";
                int orderId = -1;
                try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) orderId = rs.getInt(1);
                    }
                }

                if (orderId == -1) throw new SQLException("Failed to create order.");

                // 2. Get Item Price
                BigDecimal price = BigDecimal.ZERO;
                String priceSql = """
                SELECT Price FROM Item WHERE ID = ?
                """;
                try (PreparedStatement ps = conn.prepareStatement(priceSql)) {
                    ps.setInt(1, itemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) price = rs.getBigDecimal("Price");
                        else throw new SQLException("Item not found.");
                    }
                }

                // 3. Create OrderLine
                BigDecimal total = price.multiply(new BigDecimal(quantity));
                String lineSql = """
                INSERT INTO OrderLine (OrderID, ItemID, Quantity, Total) VALUES (?, ?, ?, ?)
                """;
                try (PreparedStatement ps = conn.prepareStatement(lineSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, itemId);
                    ps.setInt(3, quantity);
                    ps.setBigDecimal(4, total);
                    ps.executeUpdate();
                }

                // 4. Update Inventory with stored procedure
                String invSql = """
                CALL UpdateInventoryOnSale(?, ?, ?)
                """;
                try (java.sql.CallableStatement cs = conn.prepareCall(invSql)) {
                    // IN parameters
                    cs.setInt(1, itemId);
                    cs.setInt(2, quantity);

                    // OUT parameter
                    cs.registerOutParameter(3, java.sql.Types.BOOLEAN);

                    cs.execute();

                    boolean success = cs.getBoolean(3);
                    if (!success) throw new SQLException("Insufficient inventory.");
                }

                conn.commit(); // Commit Transaction
                System.out.println("Order placed successfully! Order ID: " + orderId);

            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                System.out.println("Transaction failed! Rolled back. Error: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true); // Reset
            }

        } catch (SQLException | NumberFormatException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    public static void checkItemPrice(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter Item ID: ");
            int itemId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());

            String sql = "SELECT CalculateDiscountedPrice(?, ?) AS FinalPrice";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, itemId);
                ps.setInt(2, qty);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Calculated Price: " + rs.getBigDecimal("FinalPrice"));
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error checking price: " + e.getMessage());
        }
    }

    public static void viewSalesSummary(Connection conn) {
        String query = "SELECT * FROM SalesSummary";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            System.out.println("\n--- Sales Summary ---");
            while (rs.next()) {
                System.out.printf("Customer: %s | Orders: %d | Total Spent: %s%n",
                    rs.getString("CustomerName"),
                    rs.getInt("OrderCount"),
                    rs.getBigDecimal("TotalSpent"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing summary: " + e.getMessage());
        }
    }

    public static void handleViewMenu(Connection conn, Scanner scanner) {
        System.out.println("\n--- View Data ---");
        System.out.println("1. Items");
        System.out.println("2. Categories");
        System.out.println("3. Discounts");
        System.out.println("4. Customers");
        System.out.println("5. Orders");
        System.out.println("6. Payments");
        System.out.println("7. Order Lines");
        System.out.println("8. Sales Summary (View)");
        System.out.println("9. Back");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> selectAllItems(conn);
            case "2" -> selectAllCategories(conn);
            case "3" -> selectAllDiscounts(conn);
            case "4" -> selectAllCustomers(conn);
            case "5" -> selectAllOrders(conn);
            case "6" -> selectAllPayments(conn);
            case "7" -> selectAllOrderLines(conn);
            case "8" -> viewSalesSummary(conn);
            case "9" -> {}
            default -> System.out.println("Invalid choice.");
        }
    }
}