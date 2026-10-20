# HireHub – Online Job Portal

College project: a 3-role job portal (Admin, Employer, Job Seeker) using Java 17, Maven, Servlets, JSP, JDBC, MySQL, and Apache Tomcat 9.

This repository currently includes Phases 0–2 (skeleton, JDBC, and OOP domain models). Authentication and web features come later.

## Requirements

- JDK 17
- Apache Maven 3.8+
- Apache Tomcat 9 (for deployment)
- MySQL 8 (for Phase 1 JDBC)

## Phase 1 - Database & JDBC Setup

Database name: **hirehub**

| Piece | Location |
|--------|----------|
| Table definitions + sample admin | `docs/schema.sql` |
| JDBC settings template | `src/main/resources/db.properties.example` |
| Local JDBC settings (not committed) | `src/main/resources/db.properties` |
| Connection helper | `src/main/java/com/hirehub/util/DBConnection.java` |

`DBConnection.getConnection()` reads `db.url`, `db.username`, and `db.password` from `db.properties` and uses JDBC `DriverManager`. The password is never hardcoded in Java.

### How to run `schema.sql`

1. Start MySQL locally.
2. Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties`.
3. Set `db.password` to your MySQL password (this file is gitignored).
4. Run the schema, for example:

```bash
mysql -u root -p < docs/schema.sql
```

On Windows you can also open MySQL Workbench, connect as root, and execute `docs/schema.sql`. That creates the `hirehub` database, tables, indexes, and one admin user:

- Email: `admin@hirehub.com`
- Password: `ChangeMe@123` (plain text until authentication hashing is added)

### How to run the JDBC test

```bash
mvn clean test
```

`DatabaseConnectionTest` opens a connection, asserts it is not null, and closes it. If MySQL is down or `db.properties` is missing, the test fails with a message describing those setup steps.

## Phase 2 - Core Java & OOP domain

Domain types live under `com.hirehub.model`. `User` is abstract; `Admin`, `Employer`, and `JobSeeker` inherit from it and implement `DashboardAccessible`. `UserRoleDemo` stores them in a `List<User>` and calls the overridden `getDashboardType()` method. `GenericDao<T, ID>` is the DAO contract only (no JDBC yet). `InputValidator` throws `ValidationException`.

Unit tests in `src/test/java/com/hirehub/model` and `util` do not need MySQL.

## How to build

From the project root:

```bash
mvn clean package
```

Expected artifact: `target/HireHub.war`

## How to deploy on Tomcat 9

1. Build the WAR as shown above.
2. Copy `target/HireHub.war` into Tomcat’s `webapps` folder.
3. Start Tomcat (`startup.bat` on Windows, `startup.sh` on Linux/macOS).
4. Tomcat unpacks the WAR to `webapps/HireHub`.

## How to test

Open:

[http://localhost:8080/HireHub/](http://localhost:8080/HireHub/)

You should see the HireHub welcome message. If Tomcat uses another port, change `8080` accordingly.

## Project structure

```
HireHub/
├── pom.xml
├── README.md
├── .gitignore
├── docs/schema.sql
├── src/main/java/com/hirehub/
│   ├── dao/GenericDao.java
│   ├── exception/
│   ├── model/
│   └── util/
├── src/main/resources/db.properties.example
├── src/main/webapp/WEB-INF/web.xml
├── src/main/webapp/index.jsp
└── src/test/java/com/hirehub/
```
