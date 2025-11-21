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
