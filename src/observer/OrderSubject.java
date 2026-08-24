package observer;

import java.util.ArrayList;
import java.util.List;

public class OrderSubject{

    private List<Observer> observers = new ArrayList<>();
    private String orderStatus;

    // Add observer
    public void addObserver(Observer observer){
        observers.add(observer);
    }

    // Remove observer
    public void removeObserver(Observer observer){
        observers.remove(observer);
    }

    // Update order status
    public void setOrderStatus(int orderId, String status){
        this.orderStatus = status;
        notifyObservers("Order ID " + orderId+ " status updated to: "+ status);
    }

    // Notify all observers
    private void notifyObservers(String message){
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public String getOrderStatus(){
        return orderStatus;
    }
}