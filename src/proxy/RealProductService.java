package proxy;

import database.DatabaseConnection;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RealProductService implements ProductService {

    @Override
    public void addProduct(Product product) {

        String sql ="INSERT INTO products " +"(name, category, price, stock) " +"VALUES (?, ?, ?, ?)";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement =connection.prepareStatement(sql);
            statement.setString(1,product.getName());
            statement.setString(2,product.getCategory());
            statement.setDouble(3,product.getPrice());
            statement.setInt(4,product.getStock());
            statement.executeUpdate();
            System.out.println("Product added successfully!");

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void deleteProduct(int productId) {
        String sql ="DELETE FROM products " +"WHERE product_id = ?";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement =connection.prepareStatement(sql);

            statement.setInt(1, productId);
            statement.executeUpdate();

            System.out.println("Product deleted successfully!");

        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void viewProducts() {

        String sql ="SELECT * FROM products";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement =connection.prepareStatement(sql);
            ResultSet result =statement.executeQuery();

            System.out.println("\n----- PRODUCTS -----");

            while (result.next()) {

                System.out.println(result.getInt("product_id")+ " | "+ result.getString("name")+ " | "+ result.getString("category")+ " | ₹"+ result.getDouble("price")+ " | Stock: "+ result.getInt("stock"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}