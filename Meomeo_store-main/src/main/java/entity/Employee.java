package entity;

public class Employee {
    private int id;
    private int roleId;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String barcode;
    private int status;       // 1: Đang làm, 0: Nghỉ việc tạm thời
    private int isDeleted;    // 0: bình thường, 1: đã xóa (soft delete)

    public Employee() {}

    public Employee(int id, int roleId, String username, String password,
                    String fullName, String phone, String barcode,
                    int status, int isDeleted) {
        this.id = id;
        this.roleId = roleId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.barcode = barcode;
        this.status = status;
        this.isDeleted = isDeleted;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", fullName='" + fullName + "', roleId=" + roleId + "}";
    }
}
