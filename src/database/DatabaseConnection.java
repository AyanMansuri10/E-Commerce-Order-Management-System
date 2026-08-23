package database;

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
        }catch(ClassNotFoundException e){
            System.out.println("SQLite JDBC Driver not found!");
        }catch(SQLException e){
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance(){
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
    }

    private void createTables(){
        String usersTable =
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL" +
                ")";

        String productsTable =
                "CREATE TABLE IF NOT EXISTS products (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "stock INTEGER NOT NULL" +
                ")";

        String ordersTable =
                "CREATE TABLE IF NOT EXISTS orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "total_amount REAL," +
                "status TEXT," +
                "order_type TEXT," +
                "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                ")";

        String paymentsTable =
                "CREATE TABLE IF NOT EXISTS payments (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "payment_method TEXT," +
                "amount REAL," +
                "payment_status TEXT," +
                "FOREIGN KEY(order_id) REFERENCES orders(order_id)" +
                ")";

        try (Statement statement = connection.createStatement()){
            statement.execute(usersTable);
            statement.execute(productsTable);
            statement.execute(ordersTable);
            statement.execute(paymentsTable);
            System.out.println("Tables created successfully!");

        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}