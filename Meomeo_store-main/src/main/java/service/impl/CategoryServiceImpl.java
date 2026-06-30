package service.impl;

import entity.Category;
import java.util.List;
import repository.CategoryRepository;
import service.CategoryService;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo = new CategoryRepository();

    @Override
    public List<Category> getCategoryForFilter() {
        List<Category> list = repo.getAll();
        if (list == null || list.isEmpty()) {
            return list;
        }
        return list;
    }

}
