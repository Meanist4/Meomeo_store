package entity;

public class Category {
    private int id;
    private String categoryName;
    private int isDeleted;    // 0: bình thường, 1: đã xóa (soft delete)

    public Category() {}

    public Category(int id, String categoryName, int isDeleted) {
        this.id = id;
        this.categoryName = categoryName;
        this.isDeleted = isDeleted;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }

    @Override
    public String toString() {
        return "Category{id=" + id + ", categoryName='" + categoryName + "'}";
    }
}
