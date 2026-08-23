package singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection{
    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL ="jdbc:sqlite:ecommerce.db";

    private DatabaseConnection(){
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL);
            System.out.println("Database connected successfully!");
            createTables();
            insertSampleData();
        }catch(ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        }catch(SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance(){
        if(instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
    }

    // CREATE TABLES
    private void createTables(){
        String productsTable =
                "CREATE TABLE IF NOT EXISTS products ("
                        + "product_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "product_number INTEGER UNIQUE NOT NULL, "
                        + "name TEXT NOT NULL, "
                        + "category TEXT NOT NULL, "
                        + "price REAL NOT NULL, "
                        + "stock INTEGER NOT NULL"
                        + ")";

        String ordersTable =
        "CREATE TABLE IF NOT EXISTS orders ("
                + "order_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "customer_name TEXT NOT NULL, "
                + "product_number INTEGER NOT NULL, "
                + "product_name TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL, "
                + "total_amount REAL NOT NULL, "
                + "status TEXT NOT NULL, "
                + "payment_status TEXT NOT NULL DEFAULT 'PENDING', "
                + "delivery_status TEXT NOT NULL DEFAULT 'NOT SHIPPED', "
                + "delivery_type TEXT NOT NULL DEFAULT 'STANDARD', "
                + "order_type TEXT NOT NULL, "
                + "FOREIGN KEY (product_number) "
                + "REFERENCES products(product_number)"
                + ")";

        String paymentsTable =
                "CREATE TABLE IF NOT EXISTS payments ("
                        + "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "order_id INTEGER NOT NULL, "
                        + "payment_method TEXT NOT NULL, "
                        + "amount REAL NOT NULL, "
                        + "payment_status TEXT NOT NULL, "
                        + "FOREIGN KEY (order_id) "
                        + "REFERENCES orders(order_id)"
                        + ")";

        try(Statement statement =connection.createStatement()) {
            statement.execute(productsTable);
            statement.execute(ordersTable);
            statement.execute(paymentsTable);
        }catch(SQLException e){
            System.out.println("Error creating database tables!");
            e.printStackTrace();
        }
    }

    // INSERT SAMPLE DATA
    private void insertSampleData() {

        String insertProducts =
        "INSERT OR IGNORE INTO products "
                + "(product_number, name, category, price, stock) "
                + "VALUES "
                + "(101, 'Laptop', 'Electronics', 65000, 15), "
                + "(102, 'Wireless Mouse', 'Electronics', 1200, 50), "
                + "(103, 'Shampoo', 'Personal Care', 350, 100), "
                + "(104, 'T-Shirt', 'Clothing', 799, 75), "
                + "(105, 'Headphones', 'Electronics', 2500, 30)";

        String insertOrders =
        "INSERT OR IGNORE INTO orders "
                + "(order_id, customer_name, product_number, "
                + "product_name, quantity, total_amount, status, "
                + "payment_status, delivery_status, order_type) "
                + "VALUES "
                + "(1, 'Rahul Sharma', 101, 'Laptop', 1, 65000, "
                + "'PROCESSING', 'PAID', 'SHIPPED', 'PRIORITY'), "

                + "(2, 'Priya Patel', 103, 'Shampoo', 2, 700, "
                + "'SHIPPED', 'PAID', 'DELIVERED', 'STANDARD'), "

                + "(3, 'Amit Kumar', 105, 'Headphones', 1, 2500, "
                + "'CONFIRMED', 'PENDING', 'NOT SHIPPED', 'STANDARD')";

        String insertPayments =
        "INSERT OR IGNORE INTO payments "
                + "(payment_id, order_id, payment_method, "
                + "amount, payment_status) "
                + "VALUES "
                + "(1, 1, 'UPI', 65000, 'PAID'), "
                + "(2, 2, 'CARD', 700, 'PAID'), "
                + "(3, 3, 'COD', 2500, 'PENDING')";

        try(Statement statement =connection.createStatement()){
            statement.executeUpdate(insertProducts);
            statement.executeUpdate(insertOrders);
            statement.executeUpdate(insertPayments);
        }catch(SQLException e){
            System.out.println("Error inserting initial data!");
            e.printStackTrace();
        }
    }
}