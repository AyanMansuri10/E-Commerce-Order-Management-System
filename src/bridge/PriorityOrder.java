package bridge;

public class PriorityOrder
        extends OrderType {

    public PriorityOrder(
            DeliveryMethod deliveryMethod) {

        super(deliveryMethod);
    }

    @Override
    public void processOrder(
            int orderId) {

        System.out.println(
                "Processing Priority Order: "
                + orderId
        );

        deliveryMethod.deliver(orderId);
    }
}