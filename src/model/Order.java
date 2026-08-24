package model;

public class Order {

    private int orderId;
    private String customerName;
    private String productName;
    private int quantity;
    private double totalAmount;
    private String status;
    private String orderType;

    public Order(int orderId,String customerName,String productName,int quantity,double totalAmount,String status,String orderType){
        this.orderId = orderId;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderType = orderType;
    }

    public int getOrderId(){
        return orderId;
    }

    public String getCustomerName(){
        return customerName;
    }

    public String getProductName(){
        return productName;
    }

    public int getQuantity(){
        return quantity;
    }

    public double getTotalAmount(){
        return totalAmount;
    }

    public String getStatus(){
        return status;
    }

    public String getOrderType(){
        return orderType;
    }
}