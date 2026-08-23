package bridge;

public abstract class OrderType {
    protected DeliveryMethod deliveryMethod;
    public OrderType( DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }
    public abstract void processOrder(int orderId);
}