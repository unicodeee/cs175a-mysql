-- Create table: Discount
CREATE TABLE Discount (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   Description VARCHAR(255),
   discountType VARCHAR(50),
   startDate DATE,
   endDate DATE
);


-- Create table: Category
CREATE TABLE Category (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   Name VARCHAR(100) NOT NULL,
   Description VARCHAR(255)
);


-- Create table: Item
CREATE TABLE Item (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   Name VARCHAR(100) NOT NULL,
   Price DECIMAL(10, 2) NOT NULL,
   CategoryID INT NULL,
   DiscountID INT NULL,
   FOREIGN KEY (CategoryID) REFERENCES Category(ID)
       ON DELETE SET NULL
       ON UPDATE CASCADE,
   FOREIGN KEY (DiscountID) REFERENCES Discount(ID)
       ON DELETE SET NULL
       ON UPDATE CASCADE
);


-- Create table: Supplier
CREATE TABLE Supplier (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   Name VARCHAR(100) NOT NULL,
   Email VARCHAR(100)
);


-- Create table: SuppliedBy (junction table)
CREATE TABLE SuppliedBy (
   SupplierID INT NOT NULL,
   ItemID INT NOT NULL,
   PRIMARY KEY (SupplierID, ItemID),
   FOREIGN KEY (SupplierID) REFERENCES Supplier(ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE,
   FOREIGN KEY (ItemID) REFERENCES Item(ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE
);


-- Create table: InventoryEntry
CREATE TABLE InventoryEntry (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   ItemID INT NOT NULL,
   Quantity INT NOT NULL,
   StockDate DATE NOT NULL,
   FOREIGN KEY (ItemID) REFERENCES Item(ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE
);


-- Create table: Customer
CREATE TABLE Customer (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   FirstName VARCHAR(100) NOT NULL,
   LastName VARCHAR(100) NOT NULL,
   Phone VARCHAR(20),
   Email VARCHAR(100)
);


-- Create table: Order
CREATE TABLE `Order` (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   CustomerID INT NOT NULL,
   FOREIGN KEY (CustomerID) REFERENCES Customer(ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE
);


-- Create table: Payment
CREATE TABLE Payment (
   ID INT PRIMARY KEY AUTO_INCREMENT,
   orderID INT NOT NULL,
   paymentType VARCHAR(50),
   Amount DECIMAL(10, 2),
   paymentDate DATE,
   FOREIGN KEY (orderID) REFERENCES `Order`(ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE
);


-- Create table: OrderLine
CREATE TABLE OrderLine (
   ItemID INT NOT NULL,
   OrderID INT NOT NULL,
   Total DECIMAL(10, 2) NOT NULL,
   Quantity INT NOT NULL,
   PRIMARY KEY (ItemID, OrderID),
   FOREIGN KEY (ItemID) REFERENCES Item(ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE,
   FOREIGN KEY (OrderID) REFERENCES `Order`(ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE
);






-- ====== INSERT INTO Discount ======
INSERT INTO Discount (Description, discountType, startDate, endDate) VALUES
('Summer Sale 10% off', 'Percentage', '2025-06-01', '2025-06-30'),
('Winter Clearance 20% off', 'Percentage', '2025-12-01', '2025-12-31'),
('Black Friday 50% off', 'Percentage', '2025-11-25', '2025-11-30'),
('Buy 1 Get 1 Free', 'BOGO', '2025-07-01', '2025-07-15'),
('New Customer Discount $5 off', 'Fixed', '2025-01-01', '2025-12-31');


-- ====== INSERT INTO Category ======
INSERT INTO Category (Name, Description) VALUES
('Electronics', 'Gadgets and electronic items'),
('Home Appliances', 'Appliances for household use'),
('Clothing', 'Men and women clothing'),
('Books', 'Various types of books'),
('Groceries', 'Daily grocery items');


-- ====== INSERT INTO Item ======
INSERT INTO Item (Name, Price, CategoryID, DiscountID) VALUES
('Smartphone', 699.99, 1, 1),
('Laptop', 999.99, 1, 3),
('Bluetooth Headphones', 149.99, 1, NULL),
('Microwave Oven', 250.00, 2, 2),
('Refrigerator', 850.00, 2, NULL),
('Men T-Shirt', 25.00, 3, 1),
('Women Dress', 45.00, 3, NULL),
('Cooking Oil 1L', 10.50, 5, 5),
('Rice 5kg', 15.00, 5, 5),
('Washing Machine', 650.00, 2, 2),
('Fantasy Novel', 20.00, 4, NULL),
('Cookbook', 30.00, 4, 1),
('Sneakers', 75.00, 3, 4),
('Smartwatch', 199.99, 1, 3),
('LED TV', 499.00, 1, 2),
('Blender', 80.00, 2, 1),
('Jeans', 40.00, 3, NULL),
('Cereal Pack', 8.50, 5, 5),
('Tablet', 329.99, 1, 3),
('Electric Kettle', 35.00, 2, NULL);


-- ====== INSERT INTO Supplier ======
INSERT INTO Supplier (Name, Email) VALUES
('Tech World', 'contact@techworld.com'),
('HomeGoods Inc.', 'sales@homegoods.com'),
('Fashion Hub', 'support@fashionhub.com'),
('BookNest', 'info@booknest.com'),
('FreshMart', 'hello@freshmart.com');


-- ====== INSERT INTO SuppliedBy ======
INSERT INTO SuppliedBy (SupplierID, ItemID) VALUES
(1, 1), (1, 2), (1, 3), (1, 14), (1, 19),
(2, 4), (2, 5), (2, 10), (2, 15), (2, 20),
(3, 6), (3, 7), (3, 13), (3, 17),
(4, 11), (4, 12),
(5, 8), (5, 9), (5, 18);


-- ====== INSERT INTO Customer ======
INSERT INTO Customer (FirstName, LastName, Phone, Email) VALUES
('John', 'Doe', '1234567890', 'john@example.com'),
('Jane', 'Smith', '9876543210', 'jane@example.com'),
('Michael', 'Brown', '5551112222', 'mike@example.com'),
('Emily', 'Davis', '5553334444', 'emily@example.com'),
('Chris', 'Wilson', '5556667777', 'chris@example.com');


-- ====== INSERT INTO Order ======
INSERT INTO `Order` (CustomerID) VALUES
(1), (1), (2), (3), (3), (4), (4), (5), (5), (2);


-- ====== INSERT INTO OrderLine ======
INSERT INTO OrderLine (ItemID, OrderID, Total, Quantity) VALUES
(1, 1, 699.99, 1),
(6, 1, 25.00, 1),
(4, 2, 500.00, 2),
(13, 3, 150.00, 2),
(11, 3, 20.00, 1),
(5, 4, 850.00, 1),
(9, 4, 15.00, 1),
(10, 5, 1300.00, 2),
(2, 6, 999.99, 1),
(7, 7, 90.00, 2),
(8, 8, 10.50, 1),
(18, 8, 8.50, 1),
(12, 9, 30.00, 1),
(19, 10, 329.99, 1);


-- ====== INSERT INTO Payment ======
INSERT INTO Payment (orderID, paymentType, Amount, paymentDate) VALUES
(1, 'Credit Card', 724.99, '2025-06-01'),
(2, 'Cash', 500.00, '2025-06-02'),
(3, 'PayPal', 170.00, '2025-06-03'),
(4, 'Credit Card', 865.00, '2025-06-04'),
(5, 'Debit Card', 1300.00, '2025-06-05'),
(6, 'Credit Card', 999.99, '2025-06-06'),
(7, 'Cash', 90.00, '2025-06-07'),
(8, 'Cash', 19.00, '2025-06-08'),
(9, 'PayPal', 30.00, '2025-06-09'),
(10, 'Credit Card', 329.99, '2025-06-10');


-- ====== INSERT INTO InventoryEntry ======
INSERT INTO InventoryEntry (ItemID, Quantity, StockDate) VALUES
(1, 50, '2025-05-01'),
(2, 30, '2025-05-02'),
(3, 100, '2025-05-03'),
(4, 40, '2025-05-04'),
(5, 25, '2025-05-05'),
(6, 200, '2025-05-06'),
(7, 150, '2025-05-07'),
(8, 300, '2025-05-08'),
(9, 250, '2025-05-09'),
(10, 20, '2025-05-10'),
(11, 120, '2025-05-11'),
(12, 80, '2025-05-12'),
(13, 75, '2025-05-13'),
(14, 60, '2025-05-14'),
(15, 35, '2025-05-15'),
(16, 90, '2025-05-16'),
(17, 140, '2025-05-17'),
(18, 200, '2025-05-18'),
(19, 45, '2025-05-19'),
(20, 70, '2025-05-20');




SELECT TABLE_NAME, CONSTRAINT_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'Order';


alter table OrderLine drop foreign key OrderLine_ibfk_2;


alter table Payment drop foreign key Payment_ibfk_1;


rename table `Order` to Orders;