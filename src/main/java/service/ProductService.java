package service;

import entity.Product;
import java.util.List;

public interface ProductService {

    List<Product> getPopularProducts();

    List<Product> searchProducts(String keyword);
}
