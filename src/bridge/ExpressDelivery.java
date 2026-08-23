package bridge;

public class ExpressDelivery implements DeliveryMethod {
    @Override
    public void deliver(int orderId) {
        System.out.println("Order ID " + orderId +" assigned to Express Delivery.");
    }
}