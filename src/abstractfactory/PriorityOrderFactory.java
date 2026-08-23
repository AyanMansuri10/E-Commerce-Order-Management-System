package abstractfactory;

import model.Order;

public class PriorityOrderFactory implements OrderFactory {
    @Override
    public Order createOrder(String customerName,String productName,int quantity,double amount) {
        return new Order(0,customerName,productName,quantity,amount,"CREATED","PRIORITY");
    }
}