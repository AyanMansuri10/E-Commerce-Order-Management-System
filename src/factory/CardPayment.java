package factory;

public class CardPayment implements Payment {
    @Override
    public void pay(double amount){
        System.out.println("Card Payment of " + amount + " processed successfully.");
    }
}