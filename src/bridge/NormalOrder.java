package bridge;

public class NormalOrder
        extends OrderType {

    public NormalOrder(
            DeliveryMethod deliveryMethod) {

        super(deliveryMethod);
    }

    @Override
    public void processOrder(
            int orderId) {

        System.out.println(
                "Processing Standard Order: "
                + orderId
        );

        deliveryMethod.deliver(orderId);
    }
}