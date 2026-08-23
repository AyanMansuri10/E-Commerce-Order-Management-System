package factory;

public class CODPaymentFactory extends PaymentFactory{
    @Override
    public Payment createPayment() {
        return new CODPayment();
    }
}