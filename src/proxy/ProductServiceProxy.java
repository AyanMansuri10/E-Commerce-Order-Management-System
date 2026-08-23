package proxy;

import model.Product;

public class ProductServiceProxy implements ProductService {

    private RealProductService realService;
    private boolean isAdmin;

    public ProductServiceProxy(boolean isAdmin) {
        this.realService =new RealProductService();
        this.isAdmin = isAdmin;
    }

    @Override
    public void addProduct(Product product) {
        if(isAdmin){
            realService.addProduct(product);
        }else{
            System.out.println("Access Denied! Only Admin can add products.");
        }
    }

    @Override
    public void deleteProduct(int productId) {
        if (isAdmin) {
            realService.deleteProduct(productId);
        }else{
            System.out.println("Access Denied! Only Admin can delete products.");
        }
    }

    @Override
    public void viewProducts() {
        realService.viewProducts();
    }
}