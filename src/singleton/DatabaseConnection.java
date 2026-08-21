package singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL =
            "jdbc:sqlite:ecommerce.db";

    private DatabaseConnection() {

        try {

            Class.forName("org.sqlite.JDBC");

            connection =
                    DriverManager.getConnection(URL);

            System.out.println(
                    "Database connected successfully!"
            );

            createTables();

        } catch (ClassNotFoundException e) {

            System.out.println(
                    "SQLite JDBC Driver not found!"
            );

            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println(
                    "Database connection failed!"
            );

            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {

        if (instance == null) {

            instance =
                    new DatabaseConnection();
        }

        return instance;
    }

    public Connection getConnection() {

        return connection;
    }

    private void createTables() {

        String productsTable =
                "CREATE TABLE IF NOT EXISTS products ("
                        + "product_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "name TEXT NOT NULL, "
                        + "category TEXT NOT NULL, "
                        + "price REAL NOT NULL, "
                        + "stock INTEGER NOT NULL"
                        + ")";

        String ordersTable =
                "CREATE TABLE IF NOT EXISTS orders ("
                        + "order_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "customer_name TEXT NOT NULL, "
                        + "product_name TEXT NOT NULL, "
                        + "quantity INTEGER NOT NULL, "
                        + "total_amount REAL NOT NULL, "
                        + "status TEXT NOT NULL, "
                        + "order_type TEXT NOT NULL"
                        + ")";

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(productsTable);

            statement.execute(ordersTable);

            System.out.println(
                    "Database tables created successfully!"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Error creating database tables!"
            );

            e.printStackTrace();
        }
    }
}