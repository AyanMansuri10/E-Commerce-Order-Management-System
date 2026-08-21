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

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // Singleton Pattern - Database Connection
        DatabaseConnection.getInstance();

        boolean running = true;

        System.out.println("\n==================================");
        System.out.println("   E-COMMERCE ORDER MANAGEMENT");
        System.out.println("==================================");

        while (running) {

            showMenu();

            System.out.print("Enter your choice: ");

            int choice;

            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {

                System.out.println("Please enter a valid number!");
                scanner.nextLine();
                continue;
            }

            switch (choice) {

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
                    manageProducts();
                    break;

                case 0:
                    running = false;
                    System.out.println(
                            "\nOrder Management System Closed."
                    );
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }

        scanner.close();
    }

    // =====================================================
    // MENU
    // =====================================================

    public static void showMenu() {

        System.out.println("\n------------- MENU -------------");

        System.out.println("1. Create Order");

        System.out.println("2. View All Orders");

        System.out.println("3. Update Order Status");

        System.out.println("4. Process Payment");

        System.out.println("5. Process Delivery");

        System.out.println("6. Manage Products");

        System.out.println("0. Exit");

        System.out.println("--------------------------------");
    }

    // =====================================================
    // ABSTRACT FACTORY + DATABASE
    // CREATE ORDER
    // =====================================================

    public static void createOrder() {

        System.out.println("\n----- CREATE ORDER -----");

        System.out.print("Customer Name: ");
        String customerName = scanner.nextLine();

        System.out.print("Product Name: ");
        String productName = scanner.nextLine();

        System.out.print("Quantity: ");
        int quantity = scanner.nextInt();

        System.out.print("Total Amount: ₹");
        double amount = scanner.nextDouble();

        System.out.println("\nSelect Order Type:");

        System.out.println("1. Standard Order");

        System.out.println("2. Priority Order");

        System.out.print("Choice: ");
        int choice = scanner.nextInt();

        scanner.nextLine();

        OrderFactory factory;

        if (choice == 1) {

            factory = new StandardOrderFactory();

        } else if (choice == 2) {

            factory = new PriorityOrderFactory();

        } else {

            System.out.println("Invalid order type!");
            return;
        }

        Order order = factory.createOrder(
                customerName,
                productName,
                quantity,
                amount
        );

        saveOrder(order);
    }

    // =====================================================
    // SAVE ORDER TO DATABASE
    // =====================================================

    public static void saveOrder(Order order) {

        String sql =
                "INSERT INTO orders " +
                "(customer_name, product_name, quantity, " +
                "total_amount, status, order_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {

            Connection connection =
                    DatabaseConnection
                            .getInstance()
                            .getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(
                    1,
                    order.getCustomerName()
            );

            statement.setString(
                    2,
                    order.getProductName()
            );

            statement.setInt(
                    3,
                    order.getQuantity()
            );

            statement.setDouble(
                    4,
                    order.getTotalAmount()
            );

            statement.setString(
                    5,
                    order.getStatus()
            );

            statement.setString(
                    6,
                    order.getOrderType()
            );

            int rowsAffected =
                    statement.executeUpdate();

            if (rowsAffected > 0) {

                System.out.println(
                        "\nOrder created successfully!"
                );

                System.out.println(
                        "Order Type: " +
                        order.getOrderType()
                );

                System.out.println(
                        "Status: " +
                        order.getStatus()
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error creating order!"
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // VIEW ALL ORDERS
    // =====================================================

    public static void viewOrders() {

        String sql = "SELECT * FROM orders";

        try {

            Connection connection =
                    DatabaseConnection
                            .getInstance()
                            .getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            System.out.println(
                    "\n========== ALL ORDERS =========="
            );

            boolean hasOrders = false;

            while (result.next()) {

                hasOrders = true;

                System.out.println(
                        "\nOrder ID: " +
                        result.getInt("order_id")
                );

                System.out.println(
                        "Customer: " +
                        result.getString(
                                "customer_name"
                        )
                );

                System.out.println(
                        "Product: " +
                        result.getString(
                                "product_name"
                        )
                );

                System.out.println(
                        "Quantity: " +
                        result.getInt(
                                "quantity"
                        )
                );

                System.out.println(
                        "Total Amount: ₹" +
                        result.getDouble(
                                "total_amount"
                        )
                );

                System.out.println(
                        "Status: " +
                        result.getString(
                                "status"
                        )
                );

                System.out.println(
                        "Order Type: " +
                        result.getString(
                                "order_type"
                        )
                );

                System.out.println(
                        "--------------------------"
                );
            }

            if (!hasOrders) {

                System.out.println(
                        "No orders found!"
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error retrieving orders!"
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // OBSERVER PATTERN
    // UPDATE ORDER STATUS
    // =====================================================

    public static void updateOrderStatus() {

        System.out.println(
                "\n----- UPDATE ORDER STATUS -----"
        );

        System.out.print("Enter Order ID: ");

        int orderId = scanner.nextInt();

        System.out.println(
                "\nSelect New Status:"
        );

        System.out.println("1. CONFIRMED");

        System.out.println("2. PROCESSING");

        System.out.println("3. SHIPPED");

        System.out.println("4. DELIVERED");

        System.out.print("Choice: ");

        int choice = scanner.nextInt();

        scanner.nextLine();

        String status;

        switch (choice) {

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

        String sql =
                "UPDATE orders " +
                "SET status = ? " +
                "WHERE order_id = ?";

        try {

            Connection connection =
                    DatabaseConnection
                            .getInstance()
                            .getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, status);

            statement.setInt(2, orderId);

            int rowsAffected =
                    statement.executeUpdate();

            if (rowsAffected == 0) {

                System.out.println(
                        "Order ID not found!"
                );

                return;
            }

            // Observer Pattern

            OrderSubject subject =
                    new OrderSubject();

            subject.addObserver(
                    new EmailNotification()
            );

            subject.addObserver(
                    new SMSNotification()
            );

            subject.setOrderStatus(
                    orderId,
                    status
            );

            System.out.println(
                    "Order status updated successfully!"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Error updating order status!"
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // FACTORY METHOD PATTERN
    // PROCESS PAYMENT
    // =====================================================

    public static void processPayment() {

        System.out.println(
                "\n----- PROCESS PAYMENT -----"
        );

        System.out.print("Enter Order ID: ");

        int orderId = scanner.nextInt();

        System.out.print("Payment Amount: ₹");

        double amount = scanner.nextDouble();

        System.out.println(
                "\nSelect Payment Method:"
        );

        System.out.println("1. UPI");

        System.out.println("2. Card");

        System.out.println("3. Cash On Delivery");

        System.out.print("Choice: ");

        int choice = scanner.nextInt();

        scanner.nextLine();

        PaymentFactory factory = null;

        switch (choice) {

            case 1:
                factory = new UPIPaymentFactory();
                break;

            case 2:
                factory = new CardPaymentFactory();
                break;

            case 3:
                factory = new CODPaymentFactory();
                break;

            default:

                System.out.println(
                        "Invalid payment method!"
                );

                return;
        }

        // Factory Method creates correct Payment object

        Payment payment =
                factory.createPayment();

        payment.pay(amount);

        System.out.println(
                "Payment processed successfully for Order ID: "
                        + orderId
        );
    }

    // =====================================================
    // BRIDGE PATTERN
    // PROCESS DELIVERY
    // =====================================================

    public static void processDelivery() {

        System.out.println(
                "\n----- PROCESS DELIVERY -----"
        );

        System.out.print("Enter Order ID: ");

        int orderId = scanner.nextInt();

        System.out.println(
                "\nSelect Delivery Method:"
        );

        System.out.println("1. Standard Delivery");

        System.out.println("2. Express Delivery");

        System.out.print("Choice: ");

        int deliveryChoice =
                scanner.nextInt();

        DeliveryMethod deliveryMethod;

        if (deliveryChoice == 1) {

            deliveryMethod =
                    new StandardDelivery();

        } else if (deliveryChoice == 2) {

            deliveryMethod =
                    new ExpressDelivery();

        } else {

            System.out.println(
                    "Invalid delivery method!"
            );

            scanner.nextLine();
            return;
        }

        System.out.println(
                "\nSelect Order Processing Type:"
        );

        System.out.println("1. Standard Order");

        System.out.println("2. Priority Order");

        System.out.print("Choice: ");

        int orderChoice =
                scanner.nextInt();

        scanner.nextLine();

        OrderType order;

        if (orderChoice == 1) {

            order =
                    new NormalOrder(
                            deliveryMethod
                    );

        } else if (orderChoice == 2) {

            order =
                    new PriorityOrder(
                            deliveryMethod
                    );

        } else {

            System.out.println(
                    "Invalid order type!"
            );

            return;
        }

        order.processOrder(orderId);
    }

    // =====================================================
    // PROXY PATTERN
    // PRODUCT MANAGEMENT
    // =====================================================

    public static void manageProducts() {

        System.out.println(
                "\n----- PRODUCT MANAGEMENT -----"
        );

        System.out.print(
                "Are you Admin? (yes/no): "
        );

        String answer =
                scanner.nextLine();

        boolean isAdmin =
                answer.equalsIgnoreCase("yes");

        ProductService service =
                new ProductServiceProxy(isAdmin);

        System.out.println(
                "\n1. Add Product"
        );

        System.out.println(
                "2. Delete Product"
        );

        System.out.println(
                "3. View Products"
        );

        System.out.println(
                "0. Back"
        );

        System.out.print("Choice: ");

        int choice =
                scanner.nextInt();

        scanner.nextLine();

        switch (choice) {

            case 1:

                System.out.print(
                        "Product Name: "
                );

                String name =
                        scanner.nextLine();

                System.out.print(
                        "Category: "
                );

                String category =
                        scanner.nextLine();

                System.out.print(
                        "Price: ₹"
                );

                double price =
                        scanner.nextDouble();

                System.out.print(
                        "Stock: "
                );

                int stock =
                        scanner.nextInt();

                scanner.nextLine();

                Product product =
                        new Product(
                                0,
                                name,
                                category,
                                price,
                                stock
                        );

                service.addProduct(product);

                break;

            case 2:

                System.out.print(
                        "Enter Product ID: "
                );

                int productId =
                        scanner.nextInt();

                scanner.nextLine();

                service.deleteProduct(productId);

                break;

            case 3:

                service.viewProducts();

                break;

            case 0:

                return;

            default:

                System.out.println(
                        "Invalid choice!"
                );
        }
    }
}