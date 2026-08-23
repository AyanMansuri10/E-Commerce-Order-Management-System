package abstractfactory;

import model.Order;

public interface OrderFactory {
    Order createOrder(
            String customerName,
            String productName,
            int quantity,
            double amount
    );
}