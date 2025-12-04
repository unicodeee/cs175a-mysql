# AI Development Log

## Session: November 20, 2025 - Team E3 Supermarket Database

### Issues Resolved


1. **Maven Setup**
   - Explained what Maven is and how it works
   - Created `pom.xml` with MySQL dependency
   - Set up Maven directory structure

2. **File Loading Issue**
   - Fixed `FileNotFoundException` for `app.properties`
   - Changed from `FileInputStream` to `getResourceAsStream()`

3. **MySQL Connection Failed**
   - Fixed "Connection refused" error
   - Changed `localhost:3306` to `127.0.0.1:3307`
   - Reason: Docker port mapping and Unix socket vs TCP/IP

4. **Networking Concepts**
   - Explained Unix sockets, TCP/IP, SSL
   - Why `localhost` doesn't work with Docker
   - Why `127.0.0.1` is needed

### Key Commands

```bash
# Maven
mvn clean compile
mvn exec:java -Dexec.mainClass="Main"

# Git
git add . && git commit -m "message" && git push
```



### GPT helps us fix bug in CREATE VIEW:


before, we wrote:
```shell
DROP VIEW IF EXISTS SalesSummary;
CREATE VIEW SalesSummary AS 
SELECT
   c.ID AS CustomerID,
   CONCAT(c.FirstName, ' ', c.LastName) AS CustomerName,
   COUNT(o.ID) AS OrderCount,
   SUM(ol.Total) AS TotalSpent,
   AVG(ol.Total) AS AverageOrderValue
   FROM Customer c
   LEFT JOIN Orders o ON c.ID = o.CustomerID
   LEFT JOIN OrderLine ol ON o.ID = ol.OrderID
   GROUP BY c.ID, c.FirstName, c.LastName
   ORDER BY TotalSpent DESC;
```

after:
```shell
CREATE OR REPLACE VIEW SalesSummary AS
SELECT
    c.ID AS CustomerID,
    CONCAT(c.FirstName, ' ', c.LastName) AS CustomerName,

    -- Count distinct orders
    COUNT(DISTINCT o.ID) AS OrderCount,

    -- Sum totals safely
    SUM(ol.Total) AS TotalSpent,

    -- Average order value = SUM(order totals) / COUNT(distinct orders)
    CASE 
        WHEN COUNT(DISTINCT o.ID) = 0 THEN NULL
        ELSE SUM(ol.Total) / COUNT(DISTINCT o.ID)
    END AS AverageOrderValue

FROM Customer c
LEFT JOIN Orders o ON c.ID = o.CustomerID
LEFT JOIN OrderLine ol ON o.ID = ol.OrderID
GROUP BY c.ID, c.FirstName, c.LastName
ORDER BY TotalSpent DESC;
```

prompt use:

```shell
 SELECT
    ->    c.ID AS CustomerID,
    ->    CONCAT(c.FirstName, ' ', c.LastName) AS CustomerName,
    ->    COUNT(o.ID) AS OrderCount,
    ->    SUM(ol.Total) AS TotalSpent,
    ->    AVG(ol.Total) AS AverageOrderValue
    ->    FROM Customer c
    ->    LEFT JOIN Orders o ON c.ID = o.CustomerID
    ->    LEFT JOIN OrderLine ol ON o.ID = ol.OrderID
    ->    GROUP BY c.ID, c.FirstName, c.LastName
    ->    ORDER BY TotalSpent DESC;
+------------+---------------+------------+------------+-------------------+
| CustomerID | CustomerName  | OrderCount | TotalSpent | AverageOrderValue |
+------------+---------------+------------+------------+-------------------+
|          1 | John Doe      |          5 |   34824.51 |       6964.902000 |
|          3 | Michael Brown |          3 |    2165.00 |        721.666667 |
|          4 | Emily Davis   |          2 |    1089.99 |        544.995000 |
|          2 | Jane Smith    |          3 |     499.99 |        166.663333 |
|          5 | Chris Wilson  |          3 |      49.00 |         16.333333 |
+------------+---------------+------------+------------+-------------------+
5 rows in set (0.113 sec)

mysql> select * from Orders;
+----+------------+
| ID | CustomerID |
+----+------------+
|  1 |          1 |
|  2 |          1 |
| 11 |          1 |
| 13 |          1 |
|  3 |          2 |
| 10 |          2 |
|  4 |          3 |
|  5 |          3 |
|  6 |          4 |
|  7 |          4 |
|  8 |          5 |
|  9 |          5 |
+----+------------+
12 rows in set (0.005 sec)

mysql> 

here is data context, bug in count(), likely join issues with Orderline, 
all results off by 1 for each person? how why recommend fix.
```

