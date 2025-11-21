# Team E3 - Supermarket Database

A Java application using JDBC to connect to MySQL database for supermarket management.

## Prerequisites

- Java 11 or higher
- Maven
- MySQL (running in Docker or locally)

## Installation

### Install Maven

**macOS:**
```bash
brew install maven
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install maven
```

**Windows:**
```bash
choco install maven
```

### Verify Installation

```bash
mvn --version
```

## Configuration

Update `src/main/resources/app.properties` with your MySQL connection details:

```properties
db.url=jdbc:mysql://127.0.0.1:3307/project?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

**Note:** If using Docker, make sure to use `127.0.0.1` instead of `localhost` and the correct port mapping.

## Running the Application

### Compile the Project

```bash
mvn clean compile
```

### Run the Application

```bash
mvn exec:java -Dexec.mainClass="Main"
```

### One-Liner (Compile + Run)

```bash
mvn clean compile && mvn exec:java -Dexec.mainClass="Main"
```

## Project Structure

```
cs175a-mysql/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── Main.java
│   │   └── resources/
│   │       └── app.properties
│   └── test/
│       └── java/
└── target/
```

## Troubleshooting

### Connection Refused Error

If you see `Connection refused`, check:
1. MySQL is running: `docker ps` (if using Docker)
2. Correct port in `app.properties` (use 3307 for Docker)
3. Use `127.0.0.1` instead of `localhost`

### MySQL Socket Error

```bash
# If you get socket error, force TCP connection:
mysql -h 127.0.0.1 -P 3307 -u root -p --protocol=TCP
```

## Technologies Used

- Java 11
- Maven
- MySQL 8
- JDBC (MySQL Connector/J)

## Team Members

Team E3

## License

[Add your license here]