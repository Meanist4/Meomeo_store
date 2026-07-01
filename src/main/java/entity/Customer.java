package entity;

public class Customer {

    private int id;
    private String fullName;
    private String phone;
    private boolean deleted;

    public Customer() {
    }

    public Customer(int id, String fullName, String phone, boolean deleted) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.deleted = deleted;
    }

    public Customer(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
        this.deleted = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return fullName;
    }
}