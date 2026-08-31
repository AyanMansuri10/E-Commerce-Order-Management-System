package proxy;

import model.Product;

public class ProductServiceProxy implements ProductService{
    private RealProductService realService;
    private String password;
    private static final String ADMIN_PASSWORD = "admin123";
    public ProductServiceProxy(String password) {
        this.realService = new RealProductService();
        this.password = password;
    }
    private boolean authenticate(){
        return password.equals(ADMIN_PASSWORD);
    }
    @Override
    public void addProduct(Product product){
        if(authenticate()){
            realService.addProduct(product);
        }else{
            System.out.println("Access Denied! Incorrect Admin Password.");
        }
    }

    @Override
    public void deleteProduct(int productId){
        if(authenticate()){
            realService.deleteProduct(productId);
        }else{
            System.out.println("Access Denied! Incorrect Admin Password.");
        }
    }
    
}