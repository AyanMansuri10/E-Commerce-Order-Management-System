package bridge;

public class StandardDelivery implements DeliveryMethod {
    @Override
    public void deliver(int orderId){
        System.out.println("Order ID " + orderId +" assigned to Standard Delivery.");
    }
}