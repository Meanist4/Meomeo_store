package service.impl;

import entity.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import repository.ProductRepository;
import service.ProductService;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo = new ProductRepository();

    @Override
    public List<Product> getPopularProducts() {
        List<Product> list = repo.getActiveProducts();

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream()
                .filter(p -> p.getQuantity() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPopularProducts();
        }
        return repo.searchProducts(keyword.trim());
    }
}
