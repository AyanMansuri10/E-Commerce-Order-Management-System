package proxy;

import model.Product;

public interface ProductService{
    void addProduct(Product product);
    void deleteProduct(int productId);
}