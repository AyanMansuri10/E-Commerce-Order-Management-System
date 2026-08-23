import abstractfactory.OrderFactory;
import abstractfactory.PriorityOrderFactory;
import abstractfactory.StandardOrderFactory;

import bridge.DeliveryMethod;
import bridge.ExpressDelivery;
import bridge.NormalOrder;
import bridge.OrderType;
import bridge.PriorityOrder;
import bridge.StandardDelivery;

import singleton.DatabaseConnection;

import factory.CODPaymentFactory;
import factory.CardPaymentFactory;
import factory.Payment;
import factory.PaymentFactory;
import factory.UPIPaymentFactory;

import model.Order;
import model.Product;

import observer.EmailNotification;
import observer.OrderSubject;
import observer.SMSNotification;

import proxy.ProductService;
import proxy.ProductServiceProxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Scanner;

public class Main{
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args){

        // Singleton Pattern
        DatabaseConnection.getInstance();
        boolean running = true;

        System.out.println("\n==================================");
        System.out.println("   E-COMMERCE ORDER MANAGEMENT");
        System.out.println("==================================");

        while(running){
            showMenu();
            System.out.print("Enter your choice: ");
            int choice;
            try{
                choice = scanner.nextInt();
                scanner.nextLine();
            }catch(Exception e){
                System.out.println("Please enter a valid number!");
                scanner.nextLine();
                continue;
            }
            switch(choice) {
                case 1:
                    createOrder();
                    break;
                case 2:
                    viewOrders();
                    break;
                case 3:
                    updateOrderStatus();
                    break;
                case 4:
                    processPayment();
                    break;
                case 5:
                    processDelivery();
                    break;
                case 6:
                    viewProducts();
                    break;
                case 7:
                    manageProducts();
                    break;
                case 0:
                    running = false;
                    System.out.println("\nOrder Management System Closed.");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }

    // MENU
    public static void showMenu(){
        System.out.println("\n------------- MENU -------------");
        System.out.println("1. Create Order");
        System.out.println("2. View All Orders");
        System.out.println("3. Update Order Status");
        System.out.println("4. Process Payment");
        System.out.println("5. Process Delivery");
        System.out.println("6. View Products");
        System.out.println("7. Manage Products");
        System.out.println("0. Exit");
        System.out.println("--------------------------------");
    }

    // CREATE ORDER
    // ABSTRACT FACTORY
    public static void createOrder(){
        System.out.println("\n----- CREATE ORDER -----");
        System.out.print("Customer Name: ");
        String customerName =scanner.nextLine();

        // Show available products
        viewProducts();
        System.out.print("\nEnter Product Number: ");
        int productNumber =scanner.nextInt();

        System.out.print("Quantity: ");
        int quantity =scanner.nextInt();
        scanner.nextLine();

        String productName = "";
        double price = 0;
        int stock = 0;

        // Get product from database
        String sql ="SELECT name, price, stock "+ "FROM products "+ "WHERE product_number = ?";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,productNumber);

            ResultSet result =statement.executeQuery();
            if(!result.next()){
                System.out.println("Product number not found!");
                return;
            }

            productName =result.getString("name");
            price =result.getDouble("price");
            stock =result.getInt("stock");

            if(quantity <= 0){
                System.out.println("Quantity must be greater than 0!");
                return;
            }
            if(quantity>stock){
                System.out.println("Insufficient stock!");
                System.out.println("Available stock: "+ stock);
                return;
            }
        }catch(SQLException e){
            System.out.println("Error retrieving product!");
            e.printStackTrace();
            return;
        }

        double totalAmount =price * quantity;
        System.out.println("\nProduct: "+ productName);
        System.out.println("Price per unit: Rs. "+ price);
        System.out.println("Quantity: "+ quantity);
        System.out.println("Total Amount: Rs. "+ totalAmount);

        System.out.println("\nSelect Order Type:");
        System.out.println("1. Standard Order");
        System.out.println("2. Priority Order");

        System.out.print("Choice: ");
        int choice =scanner.nextInt();
        scanner.nextLine();

        OrderFactory factory;
        if(choice == 1){
            factory =new StandardOrderFactory();
        }else if(choice == 2){
            factory =new PriorityOrderFactory();
        }else{
            System.out.println("Invalid order type!");
            return;
        }

        Order order =factory.createOrder(customerName,productName,quantity,totalAmount);
        saveOrder(order,productNumber);
    }

    // SAVE ORDER
    public static void saveOrder(Order order,int productNumber){
        String sql ="INSERT INTO orders "+ "(customer_name, product_number, "+ "product_name, quantity, total_amount, "+ "status, payment_status, "+ "delivery_status, order_type) "+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement =connection.prepareStatement(sql);
            statement.setString(1,order.getCustomerName());
            statement.setInt(2,productNumber);
            statement.setString(3,order.getProductName());
            statement.setInt(4,order.getQuantity());
            statement.setDouble(5,order.getTotalAmount());
            statement.setString(6,order.getStatus());
            statement.setString(7,"PENDING");
            statement.setString(8,"NOT SHIPPED");
            statement.setString(9,order.getOrderType());

            int rowsAffected =statement.executeUpdate();
            if (rowsAffected > 0){
                // Reduce stock
                String updateStock ="UPDATE products "+ "SET stock = stock - ? "+ "WHERE product_number = ?";
                PreparedStatement stockStatement =connection.prepareStatement(updateStock);
                stockStatement.setInt(1,order.getQuantity());
                stockStatement.setInt(2,productNumber);
                stockStatement.executeUpdate();
                System.out.println("\nOrder created successfully!");
                System.out.println("Product Number: "+ productNumber);
                System.out.println("Payment Status: PENDING");
                System.out.println("Delivery Status: NOT SHIPPED");
            }
        }catch(SQLException e) {
            System.out.println("Error creating order!");
            e.printStackTrace();
        }
    }

    public static void viewProducts(){
        String sql ="SELECT product_number, name, "+ "category, price, stock "+ "FROM products "+ "ORDER BY product_number";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement =connection.prepareStatement(sql);
            ResultSet result =statement.executeQuery();

            System.out.println("\n========== PRODUCTS ==========");
            System.out.printf("%-15s %-20s %-18s %-12s %-10s%n","Product No.","Product Name","Category","Price","Stock");
            System.out.println("------------------------------------------------------------");
            while(result.next()){
                System.out.printf("%-15d %-20s %-18s Rs.%-8.2f %-10d%n",result.getInt("product_number"),result.getString("name"),result.getString("category"),result.getDouble("price"),result.getInt("stock"));
            }
            System.out.println("------------------------------------------------------------");
        }catch(SQLException e){
            System.out.println("Error retrieving products!");
            e.printStackTrace();
        }
    }

    // VIEW ALL ORDERS
    public static void viewOrders(){
        String sql ="SELECT * FROM orders";
        try{
            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            System.out.println("\n========== ALL ORDERS ==========");
            boolean hasOrders = false;
            while (result.next()) {
                hasOrders = true;
                System.out.println("\nOrder ID: "+ result.getInt("order_id"));
                System.out.println("Customer: "+ result.getString("customer_name"));
                System.out.println("Product No.: "+ result.getInt("product_number"));
                System.out.println("Product: "+ result.getString("product_name"));
                System.out.println("Quantity: "+ result.getInt("quantity"));
                System.out.println("Total Amount: Rs. "+ result.getDouble("total_amount"));
                System.out.println("Order Status: "+ result.getString("status"));
                System.out.println("Payment Status: "+ result.getString("payment_status"));
                System.out.println("Delivery Status: "+ result.getString("delivery_status"));
                System.out.println("Order Type: "+ result.getString("order_type"));
                System.out.println("--------------------------------");
            }
            if(!hasOrders){
                System.out.println("No orders found!");
            }
        }catch(SQLException e){
            System.out.println("Error retrieving orders!");
            e.printStackTrace();
        }
    }

    // UPDATE ORDER STATUS
    // OBSERVER PATTERN
    public static void updateOrderStatus() {
        System.out.println("\n----- UPDATE ORDER STATUS -----");
        System.out.print("Enter Order ID: ");
        int orderId =scanner.nextInt();
        System.out.println("\nSelect New Status:");
        System.out.println("1. CONFIRMED");
        System.out.println("2. PROCESSING");
        System.out.println("3. SHIPPED");
        System.out.println("4. DELIVERED");
        System.out.print("Choice: ");
        int choice =scanner.nextInt();
        scanner.nextLine();
        String status;
        switch (choice){
            case 1:
                status = "CONFIRMED";
                break;
            case 2:
                status = "PROCESSING";
                break;
            case 3:
                status = "SHIPPED";
                break;
            case 4:
                status = "DELIVERED";
                break;
            default:
                System.out.println("Invalid status!");
                return;
        }
        String sql ="UPDATE orders "+ "SET status = ? "+ "WHERE order_id = ?";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement =connection.prepareStatement(sql);
            statement.setString(1,status);
            statement.setInt(2,orderId);
            int rowsAffected =statement.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Order ID not found!");
                return;
            }
            OrderSubject subject =new OrderSubject();
            subject.addObserver(new EmailNotification());
            subject.addObserver(new SMSNotification());
            subject.setOrderStatus(orderId,status);
            System.out.println("Order status updated successfully!");
        }catch(SQLException e) {
            System.out.println("Error updating order status!");
            e.printStackTrace();
        }
    }

    // PROCESS PAYMENT
    // FACTORY METHOD
    public static void processPayment() {
        System.out.println("\n----- PROCESS PAYMENT -----");
        System.out.print("Enter Order ID: ");
        int orderId =scanner.nextInt();
        scanner.nextLine();
        double amount;
        String getOrder ="SELECT total_amount, payment_status "+ "FROM orders "+ "WHERE order_id = ?";
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement =connection.prepareStatement(getOrder);
            statement.setInt(1,orderId);
            ResultSet result =statement.executeQuery();
            if(!result.next()){
                System.out.println("Order ID not found!");
                return;
            }
            amount =result.getDouble("total_amount");
            String paymentStatus =result.getString("payment_status");
            if(paymentStatus.equalsIgnoreCase("PAID")){
                System.out.println("This order has already been paid!");
                return;
            }
            System.out.println("Payment Amount: Rs. "+ amount);
        }catch(SQLException e){
            System.out.println("Error retrieving order!");
            e.printStackTrace();
            return;
        }
        System.out.println("\nSelect Payment Method:");
        System.out.println("1. UPI");
        System.out.println("2. Card");
        System.out.println("3. Cash On Delivery");
        System.out.print("Choice: ");
        int choice =scanner.nextInt();
        scanner.nextLine();

        PaymentFactory factory = null;
        String paymentMethod = "";

        switch(choice){
            case 1:
                factory =new UPIPaymentFactory();
                paymentMethod = "UPI";
                break;
            case 2:
                factory =new CardPaymentFactory();
                paymentMethod = "CARD";
                break;
            case 3:
                factory =new CODPaymentFactory();
                paymentMethod = "COD";
                break;
            default:
                System.out.println("Invalid payment method!");
                return;
        }
        Payment payment =factory.createPayment();
        payment.pay(amount);

        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            String updateOrder ="UPDATE orders "+ "SET payment_status = 'PAID' "+ "WHERE order_id = ?";

            PreparedStatement orderStatement =connection.prepareStatement(updateOrder);
            orderStatement.setInt(1,orderId);

            int rows =orderStatement.executeUpdate();
            if (rows==0){
                System.out.println("Order ID not found!");
                return;
            }
            String insertPayment ="INSERT INTO payments "+ "(order_id, payment_method, "+ "amount, payment_status) "+"VALUES (?, ?, ?, ?)";

            PreparedStatement paymentStatement =connection.prepareStatement(insertPayment);

            paymentStatement.setInt(1,orderId);
            paymentStatement.setString(2,paymentMethod);
            paymentStatement.setDouble(3,amount);

            paymentStatement.setString(4,"PAID");
            paymentStatement.executeUpdate();

            System.out.println("Payment processed successfully!");
            System.out.println("Payment Status: PAID");

        }catch(SQLException e){
            System.out.println("Error processing payment!");
            e.printStackTrace();
        }
    }

    // PROCESS DELIVERY
    // BRIDGE PATTERN
    public static void processDelivery(){
        System.out.println("\n----- PROCESS DELIVERY -----");
        System.out.print("Enter Order ID: ");
        int orderId = scanner.nextInt();

        System.out.println("\nSelect Delivery Method:");
        System.out.println("1. Standard Delivery");
        System.out.println("2. Express Delivery");
        System.out.print("Choice: ");
        int deliveryChoice =scanner.nextInt();

        DeliveryMethod deliveryMethod;
        String deliveryStatus;
        if (deliveryChoice == 1) {
            deliveryMethod = new StandardDelivery();
            deliveryStatus ="SHIPPED";
        }else if(deliveryChoice == 2) {
            deliveryMethod =new ExpressDelivery();
            deliveryStatus ="EXPRESS SHIPPED";
        }else{
            System.out.println("Invalid delivery method!");
            scanner.nextLine();
            return;
        }

        System.out.println("\nSelect Order Processing Type:");
        System.out.println("1. Standard Order");
        System.out.println("2. Priority Order");
        System.out.print("Choice: ");
        int orderChoice =scanner.nextInt();
        scanner.nextLine();

        OrderType order;
        if (orderChoice == 1) {
            order =new NormalOrder(deliveryMethod);
        }else if(orderChoice == 2) {
            order =new PriorityOrder(deliveryMethod);
        }else{
            System.out.println("Invalid order type!");
            return;
        }

        // Bridge Pattern
        order.processOrder(orderId);
        String newOrderType;
        if(orderChoice == 1){
            newOrderType = "STANDARD";
        }else{
            newOrderType = "PRIORITY";
        }
        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            String sql ="UPDATE orders "+ "SET delivery_status = ?, "+ "order_type = ? "+ "WHERE order_id = ?";
            PreparedStatement statement =connection.prepareStatement(sql);
            statement.setString(1,deliveryStatus);
            statement.setString(2,newOrderType);
            statement.setInt(3,orderId);

            int rows =statement.executeUpdate();
            if(rows == 0){
                System.out.println("Order ID not found!");
            }
        }catch(SQLException e){
            System.out.println("Error updating delivery information!");
            e.printStackTrace();
        }

        try{
            Connection connection =DatabaseConnection.getInstance().getConnection();
            String sql ="UPDATE orders "+ "SET delivery_status = ? "+ "WHERE order_id = ?";
            PreparedStatement statement =connection.prepareStatement(sql);
            statement.setString(1,deliveryStatus);
            statement.setInt(2,orderId);

            int rows =statement.executeUpdate();
            if (rows == 0){
                System.out.println("Order ID not found!");
            }
        }catch(SQLException e) {
            System.out.println("Error updating delivery status!");
            e.printStackTrace();
        }
    }

    // PRODUCT MANAGEMENT
    // PROXY PATTERN
    public static void manageProducts(){
        System.out.println("\n----- PRODUCT MANAGEMENT -----");
        System.out.print("Are you Admin? (yes/no): ");
        String answer =scanner.nextLine();

        boolean isAdmin =answer.equalsIgnoreCase("yes");
        ProductService service =new ProductServiceProxy(isAdmin);

        System.out.println("\n1. Add Product");
        System.out.println("2. Delete Product");
        System.out.println("3. View Products");
        System.out.println("0. Back");

        System.out.print("Choice: ");
        int choice =scanner.nextInt();
        scanner.nextLine();

        switch(choice){
            case 1:
                System.out.print("Product Name: ");
                String name =scanner.nextLine();
                System.out.print("Category: ");
                String category =scanner.nextLine();
                System.out.print("Price: Rs. ");
                double price =scanner.nextDouble();
                System.out.print("Stock: ");
                int stock =scanner.nextInt();
                scanner.nextLine();

                Product product =new Product(0,name,category,price,stock);
                service.addProduct(product);
                break;
            case 2:
                System.out.print("Enter Product ID: ");
                int productId =scanner.nextInt();
                scanner.nextLine();
                service.deleteProduct(productId);
                break;
            case 3:
                service.viewProducts();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }
}