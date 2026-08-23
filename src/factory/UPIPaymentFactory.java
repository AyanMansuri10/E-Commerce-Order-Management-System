package factory;

public class UPIPaymentFactory extends PaymentFactory{
    @Override
    public Payment createPayment(){
        return new UPIPayment();
    }
}