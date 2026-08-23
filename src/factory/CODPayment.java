package factory;

public class CODPayment implements Payment {
    @Override
    public void pay(double amount) {
        System.out.println("Cash on Delivery selected for " + amount);
    }
}